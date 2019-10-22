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
        end
      END

      IO.pipe do |my_stdout, child_stdout|
        IO.pipe do |my_stderr, child_stderr|
          include_path = File.dirname(Wake.method(:run).source_location.first)
          pid = Process.spawn(RbConfig.ruby, '-wU', '--disable-all', '-I', include_path, '-rwake', '-e', "Wake.run('#{workspace_path}', STDOUT)", out: child_stdout, err: child_stderr)
          child_stdout.close
          child_stderr.close
          Process.waitpid(pid)
          assert_equal '', my_stderr.read
          assert_equal <<~END, my_stdout.read
            F.

          END
        end
      end
    end
  end
end
