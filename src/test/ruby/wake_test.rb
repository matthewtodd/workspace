require 'rubygems'
require 'minitest'
require 'wake'

class WakeTest < Minitest::Test
  def skip_test_kt_jvm_test
    skip 'WIP'

    workspace do |path|
      IO.write("#{path}/BUILD", <<~END)
        kt_jvm_lib(
          name: "lib",
          srcs: ["ExampleTest.kt"],
        )

        kt_jvm_test(
          name: "ExampleTest",
          deps: ["//:lib"],
        )
      END

      IO.write("#{path}/ExampleTest.kt", <<~END)
        import kotlin.test.Test
        import kotlin.test.assertTrue

        class ExampleTest {
          @Test fun passing() {
            assertTrue(true)
          }
        }
      END

      result = wake(path)

      # How does the test framework know what to do? Look at JUnitRunner...
      # assert result.success?
      raise result.err if not result.err.empty?
    end
  end

  def test_ruby_test_runs_tests_with_isolated_load_paths
    workspace do |path|
      IO.write("#{path}/BUILD", <<~END)
        ruby_test(
          name: "SingletonPresentTest",
          srcs: ["singleton_present_test.rb"],
        )

        ruby_test(
          name: "SingletonAbsentTest",
          srcs: ["singleton_absent_test.rb"],
        )
      END

      IO.write("#{path}/singleton_present_test.rb", <<~END)
        require 'rubygems'
        require 'minitest'
        require 'singleton'

        class SingletonPresentTest < Minitest::Test
          def test_hookup
            assert Kernel.const_defined?(:Singleton),
              'Expected Singleton to be defined.'
          end
        end
      END

      IO.write("#{path}/singleton_absent_test.rb", <<~END)
        require 'rubygems'
        require 'minitest'

        class SingletonAbsentTest < Minitest::Test
          def test_hookup
            assert !Kernel.const_defined?(:Singleton),
              'Expected Singleton not to be defined.'
          end
        end
      END

      result = wake(path)
      assert result.success?, result.err
    end
  end

  def test_ruby_test_runs_tests_with_only_the_files_they_depend_on
    workspace do |path|
      IO.write("#{path}/BUILD", <<~END)
        ruby_test(
          name: 'FooTest',
          srcs: ['foo_test.rb'],
        )
      END

      IO.write("#{path}/foo_test.rb", <<~END)
        require 'rubygems'
        require 'minitest'

        class FooTest < Minitest::Test
          def test_bar_is_inaccessible
            assert_raises(LoadError) do
              require_relative('bar')
            end
          end
        end
      END

      IO.write("#{path}/bar.rb", <<~END)
        # Here I am!
      END

      result = wake(path)
      assert result.success?, result.err
    end
  end

  def test_ruby_test_exits_non_zero_when_tests_fail
    workspace do |path|
      IO.write("#{path}/BUILD", <<~END)
        ruby_test(
          name: "FailingTest",
          srcs: ["failing_test.rb"],
        )
      END

      IO.write("#{path}/failing_test.rb", <<~END)
        require 'rubygems'
        require 'minitest'

        class FailingTest < Minitest::Test
          def test_failing
            assert false
          end
        end
      END

      result = wake(path)
      assert !result.success?
    end
  end

  def test_ruby_test_exits_zero_when_tests_are_skipped
    workspace do |path|
      IO.write("#{path}/BUILD", <<~END)
        ruby_test(
          name: "SkippedTest",
          srcs: ["skipped_test.rb"],
        )
      END

      IO.write("#{path}/skipped_test.rb", <<~END)
        require 'rubygems'
        require 'minitest'

        class SkippedTest < Minitest::Test
          def test_skipping
            skip 'WIP'
          end
        end
      END

      result = wake(path)
      assert result.success?, result.err
    end
  end

  private

  def workspace
    Dir.mktmpdir("#{self.class.name}##{self.name}-") do |workspace_path|
      path = File.realpath(workspace_path)

      # ruby_test implicitly depends on //src/main/ruby:wake_testing
      # TODO reconsider something like local_repository if this gets more complicated?
      # Maybe hard-code @wake//src/main/ruby:wake_testing as the dependency, then
      # - These test sandboxes get opt/BUILD with local_repository('@wake')
      # - This main repo gets the same?
      FileUtils.mkdir_p("#{path}/src/main/ruby/wake")
      FileUtils.ln(Wake::Testing.source_location, "#{path}/src/main/ruby/wake/testing.rb", force: true)
      IO.write("#{path}/src/main/ruby/BUILD", <<~END)
        ruby_lib(
          name: 'wake_testing',
          srcs: ['wake/testing.rb'],
        )
      END

      yield path
    end
  end

  def wake(path)
    IO.pipe do |my_stdout, child_stdout|
      IO.pipe do |my_stderr, child_stderr|
        include_path = File.dirname(Wake.method(:run).source_location.first)
        pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "exit Wake.run('#{path}', [], STDOUT)", out: child_stdout, err: child_stderr)
        child_stdout.close
        child_stderr.close
        _, status = Process.waitpid2(pid)
        return WakeResult.new(my_stdout.read, my_stderr.read, status)
      end
    end
  end

  WakeResult = Struct.new(:out, :err, :status) do
    def success?
      status.exitstatus == 0
    end
  end
end
