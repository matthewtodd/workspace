require 'minitest/unit'
require 'wake'

class WakeTest < Minitest::Test
  def test_runs_all_test_rb_files
    Dir.mktmpdir do |workspace_path|
      IO.write(File.join(workspace_path, 'smoke_test.rb'), <<~END)
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

      IO.pipe do |my_stdout, child_stdout|
        IO.pipe do |my_stderr, child_stderr|
          include_path = File.dirname(Wake.method(:run).source_location.first)
          pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "srand(0); Wake.run('#{workspace_path}', STDOUT)", out: child_stdout, err: child_stderr)
          child_stdout.close
          child_stderr.close
          Process.waitpid(pid)
          assert_equal '', my_stderr.read
          assert_equal <<~END, my_stdout.read
            ES.F

              1) Error:
            SmokeTest#test_erroring:
            RuntimeError: Boom!
                #{workspace_path}/smoke_test.rb:12:in `test_erroring'

              2) Skipped:
            SmokeTest#test_skipping [#{workspace_path}/smoke_test.rb:16]:
            Skipped, no message given

              3) Failure:
            SmokeTest#test_failing [#{workspace_path}/smoke_test.rb:8]:
            Expected false to be truthy.
          END
        end
      end
    end
  end
end
