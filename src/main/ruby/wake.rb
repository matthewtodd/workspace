require 'concurrent'
require 'filesystem'
require 'shellwords'
require 'wake/actions'
require 'wake/label'
require 'wake/rules'
require 'wake/testing'

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
      workspace = Workspace.new
      @source_tree.glob('**/BUILD') do |path, contents|
        workspace.load_package(File.dirname(path), contents)
      end

      actions = Actions.new(@source_tree)
      workspace.each do |label, target|
        target.actions(actions.scoped(label))
      end

      actions.each(&:call)

      executables = ExecutableBuilder.new(workspace, @source_tree)
      workspace.each do |label, target|
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

  class Workspace
    def initialize
      @targets = {}
    end

    def load_package(path, contents)
      Rules.load(path, contents) { |target| @targets[target.label] = target }
    end

    def target(label)
      @targets.fetch(label)
    end

    def each
      # TODO topological sort?
      @targets.each do |label, target|
        yield label, target
      end
    end
  end

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
