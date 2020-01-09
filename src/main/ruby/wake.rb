require 'concurrent'
require 'filesystem'
require 'shellwords'
require 'wake/actions'
require 'wake/fetcher'
require 'wake/testing'
require 'wake/workspace'

module Wake
  def self.run(workspace_path, args, stdout)
    source_tree = Filesystem.new(workspace_path)
    executor_service = Concurrent::ExecutorService.new
    runner = Runner.new(source_tree, executor_service, stdout)
    runner.run(*args)
  end

  class Runner
    def initialize(source_tree, executor_service, stdout)
      @source_tree = source_tree
      @executor_service = executor_service
      @stdout = stdout
    end

    def run(*args)
      workspace = Workspace.new(Rules.new(Fetcher.new(@source_tree))) do |builder|
        @source_tree.glob('**/BUILD') do |path, contents|
          builder.load_package(File.dirname(path), contents)
        end
      end

      actions = Actions.new(@source_tree) do |builder|
        workspace.each do |target|
          builder.analyze(target)
        end
      end

      # TODO this shape changes when we handle actions in parallel.
      # - How to know when it's safe to proceed, i.e., when all my deps have been run?
      # - Maybe we pass the executor service to the actions?
      # - How to handle test running? Perhaps that's still a separate phase?
      actions.perform

      test_reporter = Testing::Reporter.new(@stdout)
      test_format = Testing::JsonFormat.new
      actions.each_test do |executable|
        @executor_service.submit(executable) do |single_test_result|
          test_reporter.record(test_format.load(single_test_result))
        end
      end
      @executor_service.drain
      test_reporter.report
      test_reporter.all_green?
    end
  end

end
