module Wake
  class Actions
    def initialize(filesystem)
      builder = Builder.new(filesystem)
      yield builder
      @actions = builder.build
    end

    def each(&block)
      @actions.each(&block)
    end

    class Builder
      def initialize(filesystem)
        @filesystem = filesystem
        @actions = []
        @runfiles = {}
      end

      def analyze(target)
        target.register(Scoped.new(self, target.label))
      end

      # deprecated
      def hardcoded_link(source_path, workspace_relative_path)
        link = HardcodedLink.new(@filesystem, source_path, workspace_relative_path)
        @actions << link
        link
      end

      def link(path)
        link = Link.new(@filesystem, path)
        @actions << link
        link
      end

      def runfiles(label, direct, transitive)
        @runfiles[label] = [direct, transitive]
      end

      def runfiles_for(label)
        @runfiles.fetch(label)
      end

      def test_executable(label, command, runfiles)
        test_executable = TestExecutable.new(@filesystem, label, command, runfiles)
        @actions << test_executable
        test_executable
      end

      def build
        @actions.freeze
      end
    end

    class Scoped
      def initialize(actions, label)
        @actions = actions
        @label = label
      end

      # deprecated
      def hardcoded_link(source_path, workspace_relative_path)
        @actions.hardcoded_link(source_path, workspace_relative_path)
      end

      def link(path)
        @actions.link(File.join(@label.package, path))
      end

      def runfiles(direct, transitive)
        @actions.runfiles(@label, direct, transitive)
      end

      def runfiles_for(label)
        @actions.runfiles_for(label)
      end

      def test_executable(command, runfiles)
        @actions.test_executable(@label, command, runfiles)
      end
    end

    # deprecated
    class HardcodedLink
      attr_reader :path
      attr_reader :workspace_relative_path

      def initialize(filesystem, source_path, workspace_relative_path)
        @source = source_path
        @path = filesystem.sandbox('var/tmp').absolute_path(workspace_relative_path)
        @workspace_relative_path = workspace_relative_path
      end

      def call
        FileUtils.mkdir_p(File.dirname(@path))
        FileUtils.ln(@source, @path, force: true)
      end
    end

    class Link
      attr_reader :path
      attr_reader :workspace_relative_path

      def initialize(filesystem, path)
        @source = filesystem.absolute_path(path)
        @path = filesystem.sandbox('var/tmp').absolute_path(path)
        @workspace_relative_path = path
      end

      def call
        FileUtils.mkdir_p(File.dirname(@path))
        FileUtils.ln(@source, @path, force: true)
      end
    end

    class TestExecutable
      def initialize(filesystem, label, command, runfiles)
        @executable = filesystem.sandbox('bin', label.package).absolute_path(label.name)
        @pwd = filesystem.sandbox('var/run', label.path('runfiles'))
        @runfiles = runfiles
        @command = command
        @log = filesystem.sandbox('var/log', label.path).absolute_path('stdout.log')
      end

      # TODO maybe I hang onto sandboxed-fs instead and do all this work through them
      def call
        FileUtils.mkdir_p(File.dirname(@executable))
        File.open(@executable, 'w+') { |io| io.print(script) }
        File.chmod(0755, @executable)

        @runfiles.flatten.each do |output|
          source = output.path
          target = @pwd.absolute_path(output.workspace_relative_path)
          FileUtils.mkdir_p(File.dirname(target))
          FileUtils.ln(source, target, force: true)
        end

        FileUtils.mkdir_p(File.dirname(@log))
        FileUtils.touch(@log)
      end

      private

      def script
        <<~END
          #!/bin/sh
          set -e
          cd #{@pwd.absolute_path('.')}
          exec #{@command.shelljoin} | tee #{@log}
        END
      end
    end
  end
end
