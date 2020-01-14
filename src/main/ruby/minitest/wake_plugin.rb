require 'json'

module Minitest
  module Wake
    class JsonFormat
      def dump(result)
        result.as_json.to_json
      end
    end

    # https://stackoverflow.com/questions/4922867/what-is-the-junit-xml-format-specification-that-hudson-supports/9691131#9691131
    class TestCase
      class Error < Struct.new(:type, :message, :backtrace)
        def self.json_create(object)
          new(
            object.fetch('type'),
            object.fetch('message'),
            object.fetch('backtrace')
          )
        end

        def as_json(*)
          {
            'type' => type,
            'message' => message,
            'backtrace' => backtrace,
          }
        end
      end

      class Failure < Struct.new(:message, :location)
        def self.json_create(object)
          new(
            object.fetch('message'),
            object.fetch('location')
          )
        end

        def as_json(*)
          {
            'message' => message,
            'location' => location,
          }
        end
      end

      class Skipped < Struct.new(:message, :location)
        def self.json_create(object)
          new(
            object.fetch('message'),
            object.fetch('location')
          )
        end

        def as_json(*)
          {
            'message' => message,
            'location' => location,
          }
        end
      end

      attr_reader :assertion_count
      attr_reader :error_count
      attr_reader :failure_count
      attr_reader :skip_count
      attr_reader :time

      def self.json_create(object)
        new(
          class_name: object.fetch('class_name'),
          name: object.fetch('name'),
          assertion_count: object.fetch('assertion_count'),
          time: object.fetch('time'),
          skipped: object.fetch('skipped', []).map(&Skipped.method(:json_create)),
          errors: object.fetch('errors', []).map(&Error.method(:json_create)),
          failures: object.fetch('failures', []).map(&Failure.method(:json_create)),
          system_out: object.fetch('system_out'),
          system_err: object.fetch('system_err'),
        )
      end

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

        @error_count = @errors.length
        @failure_count = @failures.length
        @skip_count = @skipped.length
      end

      def as_json(*)
        {
          'class_name' => @class_name,
          'name' => @name,
          'assertion_count' => @assertion_count,
          'time' => @time,
          'skipped' => @skipped.map(&:as_json),
          'errors' => @errors.map(&:as_json),
          'failures' => @failures.map(&:as_json),
          'system_out' => @system_out,
          'system_err' => @system_err,
        }
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

      def location
        "#{@class_name}##{@name}"
      end

      def each_error
        @errors.each { |e| yield e }
      end

      def each_failure
        @failures.each { |f| yield f }
      end

      def each_skip
        @skipped.each { |s| yield s }
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

    class Reporter < Minitest::AbstractReporter
      def initialize(io)
        @io = io
        @format = JsonFormat.new
        @translator = ResultTranslator.new
      end

      def record(result)
        @io.puts(@format.dump(@translator.call(result)))
        @io.flush
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

  class << self
    def plugin_wake_init(options)
      reporter.reporters = [Wake::Reporter.new(options[:io])]
    end
  end
end
