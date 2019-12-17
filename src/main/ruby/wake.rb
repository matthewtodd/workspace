require 'concurrent'
require 'filesystem'
require 'shellwords'
require 'wake/actions'
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
      workspace = Workspace.new do |builder|
        @source_tree.glob('**/BUILD') do |path, contents|
          builder.load_package(File.dirname(path), contents)
        end
      end

      # TODO maybe take the block-to-the-constructor builder approach here, too.
      actions = Actions.new(@source_tree)
      workspace.each do |target|
        actions.analyze(target)
      end

      # TODO this shape changes when we handle actions in parallel.
      # - How to know when it's safe to proceed, i.e., when all my deps have been run?
      # - Maybe we pass the executor service to the actions?
      # - How to handle test running? Perhaps that's still a separate phase?
      actions.each(&:call)

      # TODO this stanza goes away once the action graph is sufficient.
      executables = ExecutableBuilder.new(workspace, @source_tree)
      workspace.each do |target|
        target.accept(executables)
      end

      test_reporter = Testing::Reporter.new(@stdout)
      test_format = Testing::JsonFormat.new
      executables.each do |executable|
        @executor_service.submit(executable) do |single_test_result|
          test_reporter.record(test_format.load(single_test_result))
        end
      end
      @executor_service.drain
      test_reporter.report
      test_reporter.all_green?
    end
  end

  # TODO Visitor goes away once the action graph is sufficient.
  class Visitor
    def visit_label(label)

    end

    def visit_ruby_lib(target)
      # no-op
    end

    def visit_ruby_test(target)
      # no-op
    end
  end

  # TODO ExecutableBuilder goes away once the action graph is sufficient.
  class ExecutableBuilder < Visitor
    def initialize(workspace, source)
      @workspace = workspace
      @source = source
      @executables = []
    end

    def each
      @executables.each { |e| yield e }
    end

    def visit_ruby_test(target)
      visitor = Run.new(@workspace, @source, target)
      target.accept(visitor)
      @executables << visitor.executable
    end

    private

    class Run < Visitor
      attr_reader :executable

      def initialize(workspace, source, target)
        @workspace = workspace
        @source = source
        @bin = @source.sandbox('bin', target.label.package)
        @log = @source.sandbox('var/log', target.label.path)
        @run = @source.sandbox('var/run', target.label.path('runfiles'))
        @executable = @bin.absolute_path(target.label.name)
      end

      def visit_label(label)
        @workspace.target(label).accept(self)
      end

      def visit_ruby_lib(target)
        target.each_source do |path|
          @run.link(path, @source.absolute_path(path))
        end
      end

      def visit_ruby_test(target)
        # Implicit dependency on wake/testing.
        # TODO depend on @wake//:testing or something?
        @run.link('src/main/ruby/wake/testing.rb', Wake::Testing.source_location)

        @log.touch('stdout.log')
      end
    end
  end
end
