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
    end

    def report
      @io.puts
    end
  end
end
