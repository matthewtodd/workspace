require 'find'
require 'pathname'
require 'rbconfig'
require 'rubygems'
require 'minitest'
require 'wake/testing'

module Wake
  class Label
    def self.parse(string)
      new(*string[2..-1].split(':'))
    end

    attr_reader :package
    attr_reader :name

    def initialize(package, name)
      @package = package
      @name = name
    end

    def ==(other)
      @package == other.package && @name == other.name
    end

    def eql?(other)
      self == other
    end

    def hash
      to_s.hash
    end

    def to_s
      "//#{@package}:#{@name}"
    end

    def inspect
      to_s
    end
  end

  class KtJvmLib
    attr_reader :label

    def initialize(label:, srcs:)
      @label = label
      @srcs = srcs
    end

    def accept(visitor)

    end
  end

  class KtJvmTest
    attr_reader :label

    def initialize(label:, deps:)
      @label = label
      @deps = deps
    end

    def accept(visitor)

    end
  end

  class RubyLib
    attr_reader :label

    def initialize(label:, srcs:, deps:)
      @label = label
      @srcs = srcs
      @deps = deps
    end

    def accept(visitor)
      # no-op for now
    end

    def each_runfile(workspace)
      @deps.each do |label|
        workspace.each_runfile(label) do |path|
          yield path
        end
      end

      @srcs.each do |path|
        yield File.join(@label.package, path)
      end
    end
  end

  class RubyTest
    attr_reader :label

    def initialize(label:, srcs:, deps:[])
      @label = label
      @srcs = srcs
      @deps = deps
    end

    def accept(visitor)
      visitor.visit_test(self)
    end

    def each_runfile(workspace)
      @deps.each do |label|
        workspace.each_runfile(label) do |path|
          yield path
        end
      end

      @srcs.each do |path|
        yield File.join(@label.package, path)
      end
    end

    def test_command(resolver)
      command = [ RbConfig.ruby, '-wU', '--disable-all']
      command += ['-I', resolver.absolute_path('src/main/ruby')]
      command += ['-r', 'wake/testing']
      command += @srcs.flat_map { |src| ['-r', resolver.absolute_path(File.join(@label.package, src))] }
      command += ['-e', "Wake::Testing::Minitest.run(#{@label.name}, STDOUT)"]
      command
    end
  end

  class Workspace
    def initialize
      @targets = {}
    end

    def load_package(path, contents)
      Package.load(path, contents) { |target| @targets[target.label] = target }
    end

    def each
      # TODO topological sort?
      @targets.each_value do |target|
        yield target
      end
    end

    def each_runfile(label)
      @targets.fetch(label).each_runfile(self) do |path|
        yield path
      end
    end
  end

  class Package
    def self.load(path, contents, &collector)
      new(path, collector).instance_eval(contents)
    end

    def initialize(path, collector)
      @path = path
      @collector = collector
    end

    def kt_jvm_lib(name:, **kwargs)
      @collector.call KtJvmLib.new(label: Label.new(@path, name), **kwargs)
      self
    end

    def kt_jvm_test(name:, deps:[], **kwargs)
      @collector.call KtJvmTest.new(label: Label.new(@path, name), deps: deps.map { |string| Label.parse(string) }, **kwargs)
      self
    end

    def ruby_lib(name:, deps:[], **kwargs)
      @collector.call RubyLib.new(label: Label.new(@path, name), deps: deps.map { |string| Label.parse(string) }, **kwargs)
      self
    end

    def ruby_test(name:, deps:[], **kwargs)
      @collector.call RubyTest.new(label: Label.new(@path, name), deps: deps.map { |string| Label.parse(string) }, **kwargs)
      self
    end
  end

  class Filesystem
    def initialize(path)
      @path = path
    end

    def each_package
      Find.find(@path) do |path|
        if File.basename(path) == 'BUILD'
          yield File.dirname(path).slice(@path.length.next..-1) || '', IO.read(path)
        end
      end
    end

    def absolute_path(path)
      File.absolute_path(File.join(@path, path))
    end

    def link(path, src)
      target = absolute_path(path)
      FileUtils.mkdir_p(File.dirname(target))
      FileUtils.ln(src, target, force: true)
    end

    def sandbox(*segments)
      Filesystem.new(File.join(@path, *segments))
    end

    def runfiles_tree_for(label)
      sandbox(label.package, "#{label.name}.runfiles")
    end
  end

  def self.run(workspace_path, stdout)
    workspace = Workspace.new

    source_tree = Filesystem.new(workspace_path)
    source_tree.each_package do |path, contents|
      workspace.load_package(path, contents)
    end

    runfiles_tree = source_tree.sandbox('var/run')
    runfiles_tree_builder = RunfilesTreeBuilder.new(workspace, source_tree, runfiles_tree)
    test_runner = TestRunner.new(runfiles_tree, Executor.new, Testing::Reporter.new(stdout))

    workspace.each do |target|
      target.accept(runfiles_tree_builder)
      target.accept(test_runner)
    end

    if test_runner.run
      exit 0
    else
      exit 1
    end
  end

  class RunfilesTreeBuilder
    def initialize(workspace, source, runfiles)
      @workspace = workspace
      @source = source
      @runfiles = runfiles
    end

    def visit_test(target)
      runfiles = @runfiles.runfiles_tree_for(target.label)

      # Implicit dependency on wake/testing.
      runfiles.link('src/main/ruby/wake/testing.rb', Wake::Testing.source_location)

      target.each_runfile(@workspace) do |path|
        runfiles.link(path, @source.absolute_path(path))
      end
    end
  end

  class TestRunner
    def initialize(filesystem, pool, reporter)
      @filesystem = filesystem
      @pool = pool
      @reporter = reporter
    end

    def visit_test(target)
      @pool.execute do
        IO.pipe do |my_stdout, child_stdout|
          # binmode while we're sending marshalled data across.
          my_stdout.binmode

          pid = Process.spawn(*target.test_command(@filesystem.runfiles_tree_for(target.label)), out: child_stdout)

          child_stdout.close
          Process.waitpid(pid)
          Wake::Testing.record(my_stdout, @reporter)
        end
      end
    end

    def run
      @pool.shutdown
      @reporter.report
    end
  end

  class Executor
    def initialize
      @queue = Queue.new
      @pool = 10.times.map do
        Thread.new(@queue) do |queue|
          Thread.current.abort_on_exception = true
          while command = @queue.pop
            command.call
          end
        end
      end
    end

    def execute(&command)
      @queue << command
    end

    def shutdown
      @pool.size.times { @queue << nil }
      @pool.each(&:join)
    end
  end
end
