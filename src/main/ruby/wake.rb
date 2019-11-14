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

      executable_builder = ExecutableBuilder.new(workspace, @source_tree)
      test_runner = TestRunner.new(@source_tree.sandbox('var/run'), Executor.new, Testing::Reporter.new(@stdout))

      workspace.each do |target|
        target.accept(executable_builder)
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

    def target(label)
      @targets.fetch(label)
    end

    def each
      # TODO topological sort?
      @targets.each_value do |target|
        yield target
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

  class Visitor
    def visit_label(label)

    end

    def visit_ruby_lib(target)
      # no-op
    end

    def visit_ruby_test(target)
      # no-op
    end
  end

  class ExecutableBuilder < Visitor
    def initialize(workspace, source)
      @workspace = workspace
      @source = source
    end

    def visit_ruby_test(target)
      runfiles = @source.sandbox('var/run', target.label.package, "#{target.label.name}.runfiles")
      # TODO also pass a Bin visitor to write the script.
      target.accept(Run.new(@workspace, @source, runfiles))
      # TODO also pass a Log visitor to mkdir the log structure?
    end

    private

    class Run < Visitor
      def initialize(workspace, source, runfiles)
        @workspace = workspace
        @source = source
        @runfiles = runfiles
      end

      def visit_label(label)
        @workspace.target(label).accept(self)
      end

      def visit_ruby_lib(target)
        target.each_source do |path|
          @runfiles.link(path, @source.absolute_path(path))
        end
      end

      def visit_ruby_test(target)
        # Implicit dependency on wake/testing.
        # TODO depend on @wake//:testing or something?
        @runfiles.link('src/main/ruby/wake/testing.rb', Wake::Testing.source_location)

        target.each_source do |path|
          @runfiles.link(path, @source.absolute_path(path))
        end
      end
    end
  end

  class TestRunner < Visitor
    def initialize(filesystem, pool, reporter)
      @filesystem = filesystem
      @pool = pool
      @reporter = reporter
    end

    def visit_ruby_test(target)
      @pool.execute do
        IO.pipe do |my_stdout, child_stdout|
          # binmode while we're sending marshalled data across.
          my_stdout.binmode

          pid = Process.spawn(*target.test_command, out: child_stdout, chdir: @filesystem.runfiles_tree_for(target.label).absolute_path('.'))

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
