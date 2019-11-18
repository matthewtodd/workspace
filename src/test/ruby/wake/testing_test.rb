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

      def test_failing_diff
        assert_equal "\nfoo\n", "\nbar\n"
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

    assert_equal <<~END, output.string.lines[0..-4].join
      EFF.S

        1) Error:
      #test_erroring:
      RuntimeError: Boom!
          #{__FILE__}:11:in `test_erroring'

        2) Failure:
      #test_failing [#{__FILE__}:15]:
      Expected false to be truthy.

        3) Failure:
      #test_failing_diff [#{__FILE__}:19]:
      --- expected
      +++ actual
      @@ -1,3 +1,3 @@
       \"
      -foo
      +bar
       \"


        4) Skipped:
      #test_skipping [#{__FILE__}:27]:
      Skipped, no message given
    END

    # Finished in 0.005599s, 357.2066 runs/s, 535.8100 assertions/s.

    assert_equal <<~END, output.string.lines.last
      5 tests, 3 assertions, 2 failures, 1 error, 1 skip.
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

    lines = output.string.lines
    assert_equal "\e[31mE\e[0m\e[31mF\e[0m\e[31mF\e[0m\e[32m.\e[0m\e[33mS\e[0m", lines.first.chomp
    assert_equal "\e[1m--- expected\e[0m", lines[13].chomp
    assert_equal "\e[1m+++ actual\e[0m", lines[14].chomp
    assert_equal "\e[36m@@ -1,3 +1,3 @@\e[0m", lines[15].chomp
    assert_equal "\e[31m-foo\e[0m", lines[17].chomp
    assert_equal "\e[32m+bar\e[0m", lines[18].chomp
    assert_equal "\e[31m5 tests, 3 assertions, 2 failures, 1 error, 1 skip.\e[0m", lines.last.chomp
  end
end
