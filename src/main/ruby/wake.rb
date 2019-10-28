require 'find'
require 'pathname'
require 'rbconfig'
require 'rubygems'
require 'minitest'

module Wake
  class RubyTest
    def initialize(package:, name:, srcs:, deps:[])
      @package = package
      @name = name
      @srcs = srcs
      @deps = deps
    end

    def command
      command = [ RbConfig.ruby, '-wU', '--disable-all']
      # TODO this hardcodes wake; not all tests will test wake!
      # derive instead from the dependencies of the ruby_lib.
      command += ['-I', File.dirname(Wake.method(:run).source_location.first)]
      command += @srcs.flat_map { |src| ['-r', @package.resolve_path(src)] }
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
        #{@name}.run(MarshallingReporter.new(STDOUT), {})
        Minitest.parallel_executor.shutdown
      END
    end
  end

  class Workspace
    def initialize(path)
      @path = path
      @packages = {}
    end

    def add_package(path, contents)
      @packages[path] = Package.load(self, path, contents)
    end

    def resolve_path(path)
      File.join(@path, path)
    end

    def each
      @packages.values.each { |p| p.each { |t| yield t } }
    end
  end

  class Package
    def self.load(workspace, path, contents)
      new(workspace, path).instance_eval(contents)
    end

    def initialize(workspace, path)
      @workspace = workspace
      @path = path
      @targets = {}
    end

    def resolve_path(path)
      @workspace.resolve_path(File.join(@path, path))
    end

    def ruby_lib(name:, srcs:)
      self
    end

    def ruby_test(name:, **kwargs)
      @targets[name] = RubyTest.new(package: self, name: name, **kwargs)
      self
    end

    def each
      @targets.values.each { |t| yield t }
    end
  end

  def self.run(workspace_path, stdout)
    workspace = Workspace.new(workspace_path)

    Find.find(workspace_path) do |path|
      if File.basename(path) == 'BUILD'
        workspace.add_package(File.dirname(path).slice(workspace_path.length.next..-1) || '', IO.read(path))
      end
    end

    reporter = Reporter.new(stdout)

    ruby_tests = Queue.new
    workspace.each { |t| ruby_tests << t }

    size = 10
    pool = size.times.map {
      Thread.new(ruby_tests) do |ruby_tests|
        Thread.current.abort_on_exception = true

        while ruby_test = ruby_tests.pop
          IO.pipe do |my_stdout, child_stdout|
            # binmode while we're sending marshalled data across.
            my_stdout.binmode

            pid = Process.spawn(*ruby_test.command, out: child_stdout)

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

    size.times { ruby_tests << nil }
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
