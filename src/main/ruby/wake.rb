require 'concurrent'
require 'filesystem'
require 'shellwords'
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
      workspace.each do |target|
        target.actions(actions.scoped(target.label))
      end

      actions.each(&:call)

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

  class Actions
    def initialize(filesystem)
      @filesystem = filesystem
      @actions = []
    end

    def each(&block)
      @actions.each(&block)
    end

    def scoped(label)
      Scoped.new(self, Paths.new(@filesystem, label))
    end

    def file(path, mode, contents)
      @actions << lambda do
        FileUtils.mkdir_p(File.dirname(path))
        File.open(path, 'w+') { |io| io.print(contents) }
        File.chmod(mode, path)
      end
    end

    class Paths
      def initialize(filesystem, label)
        @filesystem = filesystem
        @label = label
      end

      def executable
        @filesystem.sandbox('bin', @label.package).absolute_path(@label.name)
      end

      def executable_log
        @filesystem.sandbox('var/log', @label.path).absolute_path('stdout.log')
      end

      def runfiles
        @filesystem.sandbox('var/run', @label.path('runfiles')).absolute_path('.')
      end
    end

    class Scoped
      def initialize(actions, paths)
        @actions = actions
        @paths = paths
      end

      def test_executable(command)
        @actions.file(@paths.executable, 0755, <<~END)
          #!/bin/sh
          set -e
          cd #{@paths.runfiles}
          exec #{command.shelljoin} | tee #{@paths.executable_log}
        END
      end

      def runfiles(direct, transitive)
        # direct is a list of label-relative paths
        # transitive is a list of labels from whom to pull outputs.
        # let's poke at it to see what we can get...
      end
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
      @targets.each_value do |target|
        yield target
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

        target.each_source do |path|
          @run.link(path, @source.absolute_path(path))
        end

        @log.touch('stdout.log')
      end
    end
  end
end
