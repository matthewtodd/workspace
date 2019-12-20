module Wake
  class Actions
    def initialize(filesystem)
      builder = Builder.new(filesystem)
      yield builder
      @actions = builder.build
    end

    def perform
      @actions.each(&:perform)
    end

    def each_test(&block)
      @actions.
        select { |action| TestExecutable === action }.
        map    { |action| action.executable }.
        each(&block)
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

      def download(url, sha256)
        download = Download.new(@filesystem, url, sha256) # TODO inject network access?
        @actions << download
        download
      end

      def extract(label, sha256, &extractor)
        extract = Extract.new(@filesystem, label, sha256, &extractor)
        @actions << extract
        extract
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

      def test_executable(label, command)
        test_executable = TestExecutable.new(@filesystem, label, command, runfiles_for(label))
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

      def download(url, sha256)
        @actions.download(url, sha256)
      end

      def extract(sha256, &extractor)
        @actions.extract(@label, sha256, &extractor)
      end

      def extracted
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

      def test_executable(command)
        @actions.test_executable(@label, command)
      end
    end

    class Download
      def initialize(filesystem, url, sha256)
      end

      def perform
      end
    end

    class Extract
      def initialize(filesystem, label, sha256, &extractor)
      end

      def perform
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

      def perform
        FileUtils.mkdir_p(File.dirname(@path))
        FileUtils.ln(@source, @path, force: true)
      end
    end

    class TestExecutable
      attr_reader :executable

      def initialize(filesystem, label, command, runfiles)
        @pwd = filesystem.sandbox('var/run', label.path('runfiles'))
        @executable = @pwd.sandbox('bin').absolute_path(label.name)
        @log = @pwd.sandbox('var/log').absolute_path(label.name)
        @runfiles = runfiles
        @command = command
      end

      # TODO maybe I hang onto sandboxed-fs instead and do all this work through them
      def perform
        FileUtils.mkdir_p(File.dirname(@executable))
        File.open(@executable, 'w+') { |io| io.print(script) }
        File.chmod(0755, @executable)

        # TODO consider sandboxing?
        # https://jmmv.dev/2019/02/sandboxfs-0-1-0.html
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
          #!/bin/bash
          set -euo pipefail
          cd #{@pwd.absolute_path('.')}
          exec #{@command.shelljoin} | tee #{@log}
        END
      end
    end
  end
end
