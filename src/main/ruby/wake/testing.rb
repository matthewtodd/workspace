module Wake
  module Testing
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
end
