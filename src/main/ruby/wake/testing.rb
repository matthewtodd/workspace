require 'base64'

module Wake
  module Testing
    def self.run(test_class, stdout)
      srand(0) # Unfortunate for test output predictability... Want to kill.
      test_class.run(Wake::Testing::WireReporter.new(stdout), {})
    end

    def self.record(pipe, reporter)
      format = MarshalFormat.new
      until pipe.eof?
        reporter.record(format.load(pipe.readline.chomp))
      end
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

    class WireReporter
      def initialize(io)
        @io = io
        @semaphore = Mutex.new
        @format = MarshalFormat.new
      end

      def prerecord(klass, name)
        # no-op
      end

      def record(result)
        @io.puts(@format.dump(result))
        @io.flush
      end

      def synchronize
        @semaphore.synchronize { yield }
      end
    end

    class MarshalFormat
      def dump(result)
        Base64.urlsafe_encode64 Marshal.dump(result)
      end

      def load(line)
        Marshal.load Base64.urlsafe_decode64(line)
      end
    end
  end
end