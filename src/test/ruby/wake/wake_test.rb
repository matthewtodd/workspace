require 'rubygems'
require 'minitest'
require 'wake'

class WakeTest < Minitest::Test
  def test_runs_all_test_rb_files
    workspace do
      IO.write('smoke_test.rb', <<~END)
        require 'rubygems'
        require 'minitest'

        class SmokeTest < Minitest::Test
          def test_passing
            assert true
          end

          def test_failing
            assert false
          end

          def test_erroring
            raise 'Boom!'
          end

          def test_skipping
            skip
          end
        end
      END

      out, err = wake

      assert_equal '', err
      assert_equal <<~END, out
        ES.F

          1) Error:
        SmokeTest#test_erroring:
        RuntimeError: Boom!
            #{Dir.pwd}/smoke_test.rb:14:in `test_erroring'

          2) Failure:
        SmokeTest#test_failing [#{Dir.pwd}/smoke_test.rb:10]:
        Expected false to be truthy.

          3) Skipped:
        SmokeTest#test_skipping [#{Dir.pwd}/smoke_test.rb:18]:
        Skipped, no message given
      END
    end
  end

  def test_runs_tests_with_isolated_load_paths
    workspace do
      IO.write('singleton_present_test.rb', <<~END)
        require 'rubygems'
        require 'minitest'
        require 'singleton'

        class SingletonPresentTest < Minitest::Test
          def test_hookup
            assert Kernel.const_defined?(:Singleton), 'Expected Singleton to be defined.'
          end
        end
      END

      IO.write('singleton_absent_test.rb', <<~END)
        require 'rubygems'
        require 'minitest'

        class SingletonAbsentTest < Minitest::Test
          def test_hookup
            assert !Kernel.const_defined?(:Singleton), 'Expected Singleton not to be defined.'
          end
        end
      END

      out, err = wake

      assert_equal '', err
      assert_equal "..\n", out
    end
  end

  private

  def workspace
    Dir.mktmpdir do |workspace_path|
      Dir.chdir(workspace_path) do
        yield
      end
    end
  end

  def wake
    IO.pipe do |my_stdout, child_stdout|
      IO.pipe do |my_stderr, child_stderr|
        include_path = File.dirname(Wake.method(:run).source_location.first)
        pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "Wake.run('#{Dir.pwd}', STDOUT)", out: child_stdout, err: child_stderr)
        child_stdout.close
        child_stderr.close
        Process.waitpid(pid)
        return my_stdout.read, my_stderr.read
      end
    end
  end
end