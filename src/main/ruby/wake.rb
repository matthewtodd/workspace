require 'find'
require 'pathname'
require 'rbconfig'
require 'rubygems'
require 'minitest'

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
  end

  class RubyLib
    attr_reader :label

    def initialize(label:, srcs:)
      @label = label
      @srcs = srcs
    end

    def accept(visitor)
      # no-op for now
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

    def test_command(resolver)
      command = [ RbConfig.ruby, '-wU', '--disable-all']
      # TODO sandboxing; this include path is meaningless for a single ruby source tree.
      # TODO want to get the include path from the dep...
      command += @deps.flat_map { |dep| ['-I', resolver.absolute_path(dep, '.')] }
      command += @srcs.flat_map { |src| ['-r', resolver.absolute_path(@label, src)] }
      command += ['-e', script]
      command
    end

    private

    def script
      return <<~END
        # binmode while we're sending marshalled data across.
        STDOUT.binmode

        # Unfortunate for test output predictability... Want to kill.
        srand(0)

        class MarshallingReporter
          def initialize(io)
            @io = io
            @semaphore = Mutex.new
          end

          def prerecord(klass, name)
            # no-op
          end

          def record(result)
            buffer = Marshal.dump(result)
            @io.puts(buffer.length)
            @io.print(buffer)
            @io.flush
          end

          def synchronize
            @semaphore.synchronize { yield }
          end
        end

        Minitest.parallel_executor = Minitest::Parallel::Executor.new(10)
        Minitest.parallel_executor.start
        #{@label.name}.run(MarshallingReporter.new(STDOUT), {})
        Minitest.parallel_executor.shutdown
      END
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
  end

  class Package
    def self.load(path, contents, &collector)
      new(path, collector).instance_eval(contents)
    end

    def initialize(path, collector)
      @path = path
      @collector = collector
    end

    def ruby_lib(name:, **kwargs)
      @collector.call RubyLib.new(label: Label.new(@path, name), **kwargs)
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

    def absolute_path(label, src)
      File.absolute_path(File.join(@path, label.package, src))
    end

    def sandbox(path)
      self
    end
  end

  def self.run(workspace_path, stdout)
    workspace = Workspace.new

    source_tree = Filesystem.new(workspace_path)
    source_tree.each_package do |path, contents|
      workspace.load_package(path, contents)
    end

    runfiles_tree = source_tree.sandbox('var/run')
    runfiles_tree_builder = RunfilesTreeBuilder.new(source_tree, runfiles_tree)
    test_runner = TestRunner.new(runfiles_tree, Executor.new, Reporter.new(stdout))

    workspace.each do |target|
      target.accept(runfiles_tree_builder)
      target.accept(test_runner)
    end

    test_runner.run
  end

  class RunfilesTreeBuilder
    def initialize(source, runfiles)

    end

    def visit_test(target)

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

          pid = Process.spawn(*target.test_command(@filesystem), out: child_stdout)

          child_stdout.close
          Process.waitpid(pid)
          until my_stdout.eof?
            length = my_stdout.readline.to_i
            buffer = my_stdout.read(length)
            result = Marshal.load(buffer)
            @reporter.record(result)
          end
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

  class Reporter
    def initialize(io)
      @io = io
      @results = []
      @semaphore = Mutex.new
    end

    def record(result)
      @semaphore.synchronize do
        @io.print result.result_code
        @io.flush
        @results << result unless result.passed?
      end
    end

    def report
      @io.puts
      @results.sort_by(&:result_code).each.with_index do |result, i|
        @io.print "\n%3d) %s" % [i+1, result]
      end
    end
  end
end
