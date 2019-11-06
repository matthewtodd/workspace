require 'rubygems'
require 'minitest'
require 'wake/testing'

class TestingTest < Minitest::Test
  def test_reporting_a_minitest_run
    test_class = Class.new(Minitest::Test) do
      i_suck_and_my_tests_are_order_dependent! # not really, but this gives predictable output

      def test_erroring
        raise 'Boom!'
      end

      def test_failing
        assert false
      end

      def test_passing
        assert true
      end

      def test_skipping
        skip
      end
    end

    pipe = StringIO.new
    Wake::Testing::Minitest.run(test_class, pipe)
    pipe.rewind

    output = StringIO.new
    reporter = Wake::Testing::Reporter.new(output)
    Wake::Testing.record(pipe, reporter)
    reporter.report

    assert_equal <<~END, output.string
      EF.S

        1) Error:
      #test_erroring:
      RuntimeError: Boom!
          #{__FILE__}:11:in `test_erroring'

        2) Failure:
      #test_failing [#{__FILE__}:15]:
      Expected false to be truthy.

        3) Skipped:
      #test_skipping [#{__FILE__}:23]:
      Skipped, no message given
    END
  end
end
