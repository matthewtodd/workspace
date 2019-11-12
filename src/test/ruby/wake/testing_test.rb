require 'rubygems'
require 'minitest'
require 'wake/testing'

class TestingTest < Minitest::Test
  def setup
    @test_class = Class.new(Minitest::Test) do
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
  end

  def test_reporting_a_minitest_run
    pipe = StringIO.new
    Wake::Testing::Minitest.run(@test_class, pipe)
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

  def test_colored_output
    pipe = StringIO.new
    Wake::Testing::Minitest.run(@test_class, pipe)
    pipe.rewind

    output = StringIO.new
    def output.tty?; true; end
    reporter = Wake::Testing::Reporter.new(output)
    Wake::Testing.record(pipe, reporter)
    reporter.report

    assert_equal "\e[31mE\e[0m\e[31mF\e[0m\e[32m.\e[0m\e[33mS\e[0m", output.string.lines.first.chomp
  end
end
