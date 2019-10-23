require 'find'
require 'rbconfig'
require 'rubygems'
require 'minitest'

module Wake
  def self.run(workspace_path, stdout)
    test_files = Queue.new

    Find.find(workspace_path) do |path|
      test_files << path if path =~ /_test.rb$/
    end

    reporter = Reporter.new(stdout)

    # TODO this hardcodes wake; not all tests will test wake!
    # derive instead from the dependencies of the ruby_lib.
    include_path = File.dirname(Wake.method(:run).source_location.first)

    # TODO also get the include path of the library itself and use that.
    size = 10
    pool = size.times.map {
      Thread.new(test_files) do |test_files|
        Thread.current.abort_on_exception = true

        while test_file = test_files.pop
          IO.pipe do |my_stdout, child_stdout|
            # binmode while we're sending marshalled data across.
            my_stdout.binmode

            pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-e', <<~END, out: child_stdout)
              # binmode while we're sending marshalled data across.
              STDOUT.binmode

              # Unfortunate for test output predictability... Want to kill.
              srand(0)

              require '#{test_file}'

              class MarshallingReporter
                def initialize(io)
                  @io = io
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
              end

              Minitest::Runnable.runnables.each do |runnable|
                runnable.run(MarshallingReporter.new(STDOUT), {})
              end
            END

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

    size.times { test_files << nil }
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
