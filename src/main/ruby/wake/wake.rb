require 'find'
require 'rubygems' # TODO don't require rubygems?
require 'minitest'

module Wake
  def self.run(workspace_path, stdout)
    reporter = Reporter.new(stdout)

    Find.find(workspace_path) do |path|
      require(path) if path =~ /_test.rb$/
    end

    reporter.start

    Minitest::Runnable.runnables.each do |runnable|
      runnable.run(reporter, {})
    end

    reporter.report
  end

  class Reporter
    def initialize(io)
      @io = io
      @results = []
    end

    def start
      # no-op
    end

    def prerecord(klass, name)
      # no-op
    end

    def record(result)
      @io.print result.result_code
      @io.flush

      @results << result unless result.passed?
    end

    def report
      @io.puts
      @results.sort_by(&:result_code).each.with_index do |result, i|
        @io.print "\n%3d) %s" % [i+1, result]
      end
    end
  end
end
