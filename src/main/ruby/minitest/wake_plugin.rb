require 'json'

module Minitest
  module Wake
    class Reporter < Minitest::AbstractReporter
      def initialize(io)
        @io = io
      end

      def record(result)
        @io.puts(translate(result).to_json)
        @io.flush
      end

      private

      def translate(result)
        {
          'class_name' => result.respond_to?(:class_name) ?
              result.class_name :
              result.class.name,
          'name' => result.name,
          'assertion_count' => result.assertions,
          'time' => result.time,
          'errors' => result.failures.
              select { |failure| failure.result_label == 'Error' }.
              map { |failure| {
                'type' => failure.error.class.name,
                'message' => failure.error.message,
                'backtrace' => ::Minitest.filter_backtrace(failure.error.backtrace),
              }},
          'failures' => result.failures.
              select { |failure| failure.result_label == 'Failure' }.
              map { |failure| {
                'message' => failure.error.message,
                'location' => failure.location,
              }},
          'skipped' => result.failures.
              select { |failure| failure.result_label == 'Skipped' }.
              map { |failure| {
                'message' => failure.message,
                'location' => failure.location,
              }},
          'system_out' => '',
          'system_err' => '',
        }
      end
    end
  end

  class << self
    def plugin_wake_init(options)
      reporter.reporters = [Wake::Reporter.new(options[:io])]
    end
  end
end
