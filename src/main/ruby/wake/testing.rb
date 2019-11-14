require 'json'

module Wake
  module Testing
    def self.record(pipe, reporter)
      format = JsonFormat.new
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
        @semaphore = Mutex.new
        @summarizer = Summarizer.new
      end

      def record(result)
        @semaphore.synchronize do
          @io.print maybe_color_result_code(result.result_code)
          @io.flush
          @summarizer.record(result)
        end
      end

      def report
        @io.puts
        @summarizer.each.with_index do |result, i|
          @io.print "\n%3d) %s" % [i+1, result]
        end
        @io.puts
        @io.puts @summarizer.timing
        @io.puts maybe_color(@summarizer.counts, @summarizer.success? ? GREEN : RED)
        @summarizer.success?
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

      class Summarizer
        def initialize
          @test_count = 0
          @assertion_count = 0
          @error_count = 0
          @failure_count = 0
          @skip_count = 0
          @total_time = 0
          @errors_failures_skips = []
        end

        def record(result)
          @test_count += 1
          @assertion_count += result.assertion_count
          @error_count += result.error_count
          @failure_count += result.failure_count
          @skip_count += result.skip_count
          @total_time += result.time
          @errors_failures_skips << result unless result.passed?
        end

        def each
          @errors_failures_skips.sort_by(&:result_code).each
        end

        def timing
          "Finished in %.6fs, %.4f runs/s, %.4f assertions/s." %
            [@total_time, @test_count / @total_time, @assertion_count / @total_time]
        end

        def counts
          counts = []
          counts << pluralize(@test_count, 'test')
          counts << pluralize(@assertion_count, 'assertion')
          counts << pluralize(@failure_count, 'failure')
          counts << pluralize(@error_count, 'error')
          counts << pluralize(@skip_count, 'skip')
          counts.join(', ').concat('.')
        end

        def success?
          @errors_failures_skips.all?(&:skipped?)
        end

        private

        def pluralize(count, string)
          count == 1 ? "#{count} #{string}" : "#{count} #{string}s"
        end
      end
    end

    class JsonFormat
      def dump(result)
        result.as_json.to_json
      end

      def load(line)
        TestCase.json_create(JSON.parse(line))
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
          @format = JsonFormat.new
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
