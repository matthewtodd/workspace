require 'base64'

module Wake
  module Testing
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
        @errors_failures_skips = []
        @semaphore = Mutex.new
      end

      def record(result)
        @semaphore.synchronize do
          @io.print maybe_color_result_code(result.result_code)
          @io.flush
          @errors_failures_skips << result unless result.passed?
        end
      end

      def report
        @io.puts
        @errors_failures_skips.sort_by(&:result_code).each.with_index do |result, i|
          @io.print "\n%3d) %s" % [i+1, result]
        end
        @errors_failures_skips.all?(&:skipped?)
      end

      private

      RED = 31
      GREEN = 32
      YELLOW = 33

      RESULT_CODE_COLORS = {
        'E' => RED,
        'F' => RED,
        'S' => YELLOW,
        '.' => GREEN
      }.freeze

      def maybe_color_result_code(result_code)
        maybe_color(result_code, RESULT_CODE_COLORS.fetch(result_code))
      end

      def maybe_color(string, color)
        @io.tty? ? "\e[#{color}m#{string}\e[0m" : string
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

    # https://stackoverflow.com/questions/4922867/what-is-the-junit-xml-format-specification-that-hudson-supports/9691131#9691131
    class TestCase
      Error = Struct.new(:type, :message, :backtrace)
      Failure = Struct.new(:message, :location)
      Skipped = Struct.new(:message, :location)

      def initialize(**kwargs)
        @class_name = kwargs.fetch(:class_name)
        @name = kwargs.fetch(:name)
        @assertion_count = kwargs.fetch(:assertion_count)
        @time = kwargs.fetch(:time)
        @skipped = kwargs.fetch(:skipped)
        @errors = kwargs.fetch(:errors)
        @failures = kwargs.fetch(:failures)
        @system_out = kwargs.fetch(:system_out)
        @system_err = kwargs.fetch(:system_err)
      end

      def passed?
        @skipped.empty? && @errors.empty? && @failures.empty?
      end

      def skipped?
        !@skipped.empty? && @errors.empty? && @failures.empty?
      end

      def result_code
        if @skipped.any?
          'S'
        elsif @errors.any?
          'E'
        elsif @failures.any?
          'F'
        else
          '.'
        end
      end

      def to_s
        string = ''

        @errors.each do |error|
          string += "Error:\n"
          string += "#{@class_name}##{@name}:\n"
          string += "#{error.type}: #{error.message}\n"
          string += "    #{error.backtrace.join("    \n")}\n"
        end

        @failures.each do |failure|
          string += "Failure:\n"
          string += "#{@class_name}##{@name} [#{failure.location}]:\n"
          string += "#{failure.message}\n"
        end

        @skipped.each do |skipped|
          string += "Skipped:\n"
          string += "#{@class_name}##{@name} [#{skipped.location}]:\n"
          string += "#{skipped.message}\n"
        end

        string
      end

      class Builder
        def initialize
          @class_name = 'UNSET CLASS NAME'
          @name = 'UNSET NAME'
          @assertion_count = 0
          @time = 0
          @skipped = []
          @errors = []
          @failures = []
          @system_out = ''
          @system_err = ''
        end

        def class_name(class_name)
          @class_name = class_name
          self
        end

        def name(name)
          @name = name
          self
        end

        def assertion_count(assertion_count)
          @assertion_count = assertion_count
          self
        end

        def time(time)
          @time = time
          self
        end

        def error(type, message, backtrace)
          @errors << Error.new(type, message, backtrace)
          self
        end

        def failure(message, location)
          @failures << Failure.new(message, location)
          self
        end

        def skipped(message, location)
          @skipped << Skipped.new(message, location)
          self
        end

        def build
          TestCase.new(
            class_name: @class_name,
            name: @name,
            assertion_count: @assertion_count,
            time: @time,
            skipped: @skipped,
            errors: @errors,
            failures: @failures,
            system_out: @system_out,
            system_err: @system_err
          )
        end
      end
    end

    module Minitest
      def self.run(test_class, stdout)
        test_class.run(WireReporter.new(stdout), {})
      end

      class WireReporter
        def initialize(io)
          @io = io
          @semaphore = Mutex.new
          @format = MarshalFormat.new
          @translator = ResultTranslator.new
        end

        def prerecord(klass, name)
          # no-op
        end

        def record(result)
          @io.puts(@format.dump(@translator.call(result)))
          @io.flush
        end

        def synchronize
          @semaphore.synchronize { yield }
        end
      end

      class ResultTranslator
        def call(result)
          test_case = TestCase::Builder.new
            .class_name(result.respond_to?(:class_name) ? result.class_name : result.class.name)
            .name(result.name)
            .assertion_count(result.assertions)
            .time(result.time)

          result.failures.each do |failure|
            case failure.result_label
            when 'Error'
              test_case.error(failure.error.class.name, failure.error.message, ::Minitest.filter_backtrace(failure.error.backtrace))
            when 'Failure'
              test_case.failure(failure.error.message, failure.location)
            when 'Skipped'
              test_case.skipped(failure.message, failure.location)
            end
          end

          test_case.build
        end
      end
    end
  end
end
