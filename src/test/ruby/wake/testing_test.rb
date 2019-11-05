require 'rubygems'
require 'minitest'
require 'wake/testing'

class TestingTest < Minitest::Test
  include Wake::Testing

  parallelize_me!

  def test_reporting_a_minitest_run
    test_class = Class.new(Minitest::Test) do
      def test_failing
        assert false
      end
    end

    io = StringIO.new
    reporter = Reporter.new(io)
    test_class.run(reporter, {})
    reporter.report
    assert_equal <<~END, io.string
      F

        1) Failure:
      #test_failing [#{__FILE__}:13]:
      Expected false to be truthy.
    END
  end
end
