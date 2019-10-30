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

    def include_path(resolver)
      resolver.path(@label, '.')
    end
  end

  class RubyTest
    attr_reader :label

    def initialize(label:, srcs:, deps:[])
      @label = label
      @srcs = srcs
      @deps = deps
    end

    def test_command(resolver)
      command = [ RbConfig.ruby, '-wU', '--disable-all']
      # TODO sandboxing; this include path is meaningless for a single ruby source tree.
      command += @deps.flat_map { |dep| ['-I', resolver.target(dep).include_path(resolver)] }
      command += @srcs.flat_map { |src| ['-r', resolver.path(@label, src)] }
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
    def initialize(path)
      @path = path
      @targets = {}
    end

    def load_package(build_file_path, contents)
      path = File.dirname(build_file_path).slice(@path.length.next..-1) || ''
      Package.load(path, contents) { |target| @targets[target.label] = target }
    end

    def target(label)
      @targets[label]
    end

    def path(label, src)
      File.absolute_path(File.join(@path, label.package, src))
    end

    def test
      @targets.each_value do |target|
        if target.respond_to?(:test_command)
          yield target.test_command(self)
        end
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

  def self.run(workspace_path, stdout)
    workspace = Workspace.new(workspace_path)

    Find.find(workspace_path) do |path|
      if File.basename(path) == 'BUILD'
        workspace.load_package(path, IO.read(path))
      end
    end

    reporter = Reporter.new(stdout)

    test_commands = Queue.new
    workspace.test { |test_command| test_commands << test_command }

    size = 10
    pool = size.times.map {
      Thread.new(test_commands) do |test_commands|
        Thread.current.abort_on_exception = true

        while test_command = test_commands.pop
          IO.pipe do |my_stdout, child_stdout|
            # binmode while we're sending marshalled data across.
            my_stdout.binmode

            pid = Process.spawn(*test_command, out: child_stdout)

            child_stdout.close
            Process.waitpid(pid)
            until my_stdout.eof?
              length = my_stdout.readline.to_i
              buffer = my_stdout.read(length)
              result = Marshal.load(buffer)
              reporter.record(result)
            end
          end
        end
      end
    }

    size.times { test_commands << nil }
    pool.each(&:join)
    reporter.report
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
