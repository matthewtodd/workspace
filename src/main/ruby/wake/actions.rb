module Wake
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
      # TODO should we pass these actions through the filesystem? Not yet sure what meaningful test we'd find in-memory...
      @actions << lambda do
        FileUtils.mkdir_p(File.dirname(path))
        File.open(path, 'w+') { |io| io.print(contents) }
        File.chmod(mode, path)
      end
    end

    def link(source, target)
      @actions << lambda do
        FileUtils.mkdir_p(File.dirname(target))
        FileUtils.ln(source, target, force: true)
      end
    end

    class Paths
      def initialize(filesystem, label)
        @filesystem = filesystem
        @label = label
      end

      def executable # TODO I think this new pattern means we don't need sandboxes so much.
        @filesystem.sandbox('bin', @label.package).absolute_path(@label.name)
      end

      def executable_log
        @filesystem.sandbox('var/log', @label.path).absolute_path('stdout.log')
      end

      def runfiles
        @filesystem.sandbox('var/run', @label.path('runfiles')).absolute_path('.')
      end

      def package_relative_output(path)
        @filesystem.sandbox('var/tmp', @label.package).absolute_path(path)
      end

      def package_relative_runfiles(path)
        @filesystem.sandbox('var/run', @label.path('runfiles'), @label.package).absolute_path(path)
      end

      def package_relative_source(path)
        @filesystem.sandbox(@label.package).absolute_path(path)
      end
    end

    class Scoped
      def initialize(actions, paths)
        @actions = actions
        @paths = paths
      end

      def output_links(paths)
        paths.each do |path|
          @actions.link(
            @paths.package_relative_source(path),
            @paths.package_relative_output(path)
          )
        end
      end

      def test_executable(command, direct, transitive)
        @actions.file(@paths.executable, 0755, <<~END)
          #!/bin/sh
          set -e
          cd #{@paths.runfiles}
          exec #{command.shelljoin} | tee #{@paths.executable_log}
        END

        # direct is a list of label-relative paths
        # transitive is a list of labels from whom to pull outputs.
        # let's poke at it to see what we can get...
        direct.each do |path|
          @actions.link(
            @paths.package_relative_output(path),
            @paths.package_relative_runfiles(path)
          )
        end
      end
    end
  end
end