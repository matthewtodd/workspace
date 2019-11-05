require 'rubygems'
require 'minitest'
require 'wake/testing'

class TestingTest < Minitest::Test
  include Wake::Testing

  def test_reporting_a_minitest_run
    test_class = Class.new(Minitest::Test) do
      def test_failing
        assert false
      end
    end

    pipe = StringIO.new
    Wake::Testing.run(test_class, pipe)
    pipe.rewind

    io = StringIO.new
    reporter = Reporter.new(io)
    until pipe.eof?
      length = pipe.readline.to_i
      buffer = pipe.read(length)
      result = Marshal.load(buffer)
      reporter.record(result)
    end
    reporter.report

    assert_equal <<~END, io.string
      F

        1) Failure:
      #test_failing [#{__FILE__}:11]:
      Expected false to be truthy.
    END
  end
end
