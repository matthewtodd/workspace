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

    output = StringIO.new
    reporter = Reporter.new(output)
    Wake::Testing.record(pipe, reporter)
    reporter.report

    assert_equal <<~END, output.string
      F

        1) Failure:
      #test_failing [#{__FILE__}:11]:
      Expected false to be truthy.
    END
  end
end
