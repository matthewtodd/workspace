require 'minitest/autorun'
require 'wake'

class WakeTest < Minitest::Test
  def test_ruby_test_runs_tests_with_isolated_load_paths
    workspace do |path|
      IO.write("#{path}/src/BUILD", <<~END)
        ruby_test(
          name: "SingletonPresentTest",
          srcs: ["singleton_present_test.rb"],
          deps: ["//src/minitest:wake_plugin"],
        )

        ruby_test(
          name: "SingletonAbsentTest",
          srcs: ["singleton_absent_test.rb"],
          deps: ["//src/minitest:wake_plugin"],
        )
      END

      IO.write("#{path}/src/singleton_present_test.rb", <<~END)
        require 'rubygems'
        require 'minitest/autorun'
        require 'singleton'

        class SingletonPresentTest < Minitest::Test
          def test_hookup
            assert Kernel.const_defined?(:Singleton),
              'Expected Singleton to be defined.'
          end
        end
      END

      IO.write("#{path}/src/singleton_absent_test.rb", <<~END)
        require 'rubygems'
        require 'minitest/autorun'

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
      IO.write("#{path}/src/BUILD", <<~END)
        ruby_test(
          name: 'FooTest',
          srcs: ['foo_test.rb'],
          deps: ["//src/minitest:wake_plugin"],
        )
      END

      IO.write("#{path}/src/foo_test.rb", <<~END)
        require 'rubygems'
        require 'minitest/autorun'

        class FooTest < Minitest::Test
          def test_bar_is_inaccessible
            assert_raises(LoadError) do
              require_relative('bar')
            end
          end
        end
      END

      IO.write("#{path}/src/bar.rb", <<~END)
        # Here I am!
      END

      result = wake(path)
      assert result.success?, result.err
    end
  end

  def test_ruby_test_exits_non_zero_when_tests_fail
    workspace do |path|
      IO.write("#{path}/src/BUILD", <<~END)
        ruby_test(
          name: "FailingTest",
          srcs: ["failing_test.rb"],
          deps: ["//src/minitest:wake_plugin"],
        )
      END

      IO.write("#{path}/src/failing_test.rb", <<~END)
        require 'rubygems'
        require 'minitest/autorun'

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
      IO.write("#{path}/src/BUILD", <<~END)
        ruby_test(
          name: "SkippedTest",
          srcs: ["skipped_test.rb"],
          deps: ["//src/minitest:wake_plugin"],
        )
      END

      IO.write("#{path}/src/skipped_test.rb", <<~END)
        require 'rubygems'
        require 'minitest/autorun'

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
    path = File.realpath(Dir.mktmpdir("#{self.class.name}##{self.name}-"))

    begin
      FileUtils.mkdir_p("#{path}/src/minitest")
      FileUtils.ln(File.expand_path('../../../main/ruby/minitest/wake_plugin.rb', __FILE__), "#{path}/src/minitest/wake_plugin.rb", force: true)
      IO.write("#{path}/src/minitest/BUILD", <<~END)
        ruby_lib(
          name: 'wake_plugin',
          srcs: ['wake_plugin.rb'],
          load_path: '..',
        )
      END

      yield path
    ensure
      FileUtils.remove_entry(path)
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
