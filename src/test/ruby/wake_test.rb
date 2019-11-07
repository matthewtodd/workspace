require 'rubygems'
require 'minitest'
require 'wake'

class WakeTest < Minitest::Test
  def test_runs_tests_with_isolated_load_paths
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

      assert result.success?
      raise result.err if not result.err.empty?
      assert_equal "..\n", result.out
    end
  end

  def test_runs_tests_with_only_the_files_they_depend_on
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

      assert result.success?
      raise result.err if not result.err.empty?
      assert_equal ".\n", result.out
    end
  end

  def test_exits_non_zero_when_tests_fail
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

  private

  def workspace
    Dir.mktmpdir("#{self.class.name}##{self.name}-") do |workspace_path|
      yield File.realpath(workspace_path)
    end
  end

  def wake(path)
    IO.pipe do |my_stdout, child_stdout|
      IO.pipe do |my_stderr, child_stderr|
        include_path = File.dirname(Wake.method(:run).source_location.first)
        pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "Wake.run('#{path}', STDOUT)", out: child_stdout, err: child_stderr)
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
