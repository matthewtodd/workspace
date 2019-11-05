module Wake
  module Testing
    def self.run(test_class, stdout)
      # binmode while we're sending marshalled data across.
      stdout.binmode

      # Unfortunate for test output predictability... Want to kill.
      srand(0)

      Minitest.parallel_executor = Minitest::Parallel::Executor.new(10)
      Minitest.parallel_executor.start
      test_class.run(Wake::Testing::MarshallingReporter.new(stdout), {})
      Minitest.parallel_executor.shutdown
    end

    def self.source_location
      method(:source_location).source_location.first
    end

    class Reporter
      def initialize(io)
        @io = io
        @results = []
        @semaphore = Mutex.new
      end

      def prerecord(*args)

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
  end
end
