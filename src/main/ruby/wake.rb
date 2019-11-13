require 'fileutils'
require 'find'
require 'wake/rules'
require 'wake/testing'

module Wake
  def self.run(workspace_path, stdout)
    source_tree = Filesystem.new(workspace_path)
    runner = Runner.new(source_tree, stdout)
    exit runner.run ? 0 : 1
  end

  class Runner
    def initialize(source_tree, stdout)
      @source_tree = source_tree
      @stdout = stdout
    end

    def run
      workspace = Workspace.new
      @source_tree.each_package do |path, contents|
        workspace.load_package(path, contents)
      end

      runfiles_tree = @source_tree.sandbox('var/run')
      runfiles_tree_builder = RunfilesTreeBuilder.new(workspace, @source_tree, runfiles_tree)
      test_runner = TestRunner.new(runfiles_tree, Executor.new, Testing::Reporter.new(@stdout))

      workspace.each do |target|
        target.accept(runfiles_tree_builder)
        target.accept(test_runner)
      end

      test_runner.run
    end
  end

  class Workspace
    def initialize
      @targets = {}
    end

    def load_package(path, contents)
      Rules.load(path, contents) { |target| @targets[target.label] = target }
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
end
