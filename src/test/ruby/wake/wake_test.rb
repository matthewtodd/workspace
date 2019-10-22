require 'minitest/unit'
require 'wake'

class WakeTest < Minitest::Test
  def test_runs_all_test_rb_files
    workspace do
      IO.write('smoke_test.rb', <<~END)
        require 'minitest/unit'

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

      wake do |stdout, stderr|
        assert_equal '', stderr.read
        assert_equal <<~END, stdout.read
          ES.F

            1) Error:
          SmokeTest#test_erroring:
          RuntimeError: Boom!
              #{Dir.pwd}/smoke_test.rb:13:in `test_erroring'

            2) Skipped:
          SmokeTest#test_skipping [#{Dir.pwd}/smoke_test.rb:17]:
          Skipped, no message given

            3) Failure:
          SmokeTest#test_failing [#{Dir.pwd}/smoke_test.rb:9]:
          Expected false to be truthy.
        END
      end
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
        pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "srand(0); Wake.run('#{Dir.pwd}', STDOUT)", out: child_stdout, err: child_stderr)
        child_stdout.close
        child_stderr.close
        Process.waitpid(pid)
        yield my_stdout, my_stderr
      end
    end
  end
end
