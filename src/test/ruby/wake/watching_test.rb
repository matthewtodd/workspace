require 'rubygems'
require 'minitest'
require 'wake'

class WatchingTest < Minitest::Test
  def test_watch
    source_tree = Object.new
    def source_tree.glob(pattern)
    end

    executor_service = Object.new
    def executor_service.shutdown
    end

    stdout = StringIO.new
    runner = Wake::Runner.new(source_tree, executor_service, stdout)
    runner.run
  end
end
