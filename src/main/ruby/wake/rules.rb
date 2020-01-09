require 'rbconfig'
require 'rubygems'
require 'rubygems/package'
require 'wake/label'

module Wake
  class Rules
    def initialize(fetcher)
      @fetcher = fetcher
    end

    def load(path, contents, &collector)
      Dsl.new(path, @fetcher, collector).instance_eval(contents)
    end

    class Dsl
      def initialize(path, fetcher, collector)
        @path = path
        @fetcher = fetcher
        @collector = collector
      end

      def kt_jvm_lib(name:, srcs:)
        @collector.call KtJvmLib.new(label: label(name), srcs: srcs)
        self
      end

      def kt_jvm_test(name:, deps:[])
        @collector.call KtJvmTest.new(label: label(name), deps: parse(deps))
        self
      end

      def ruby_gem(name:, version:, sha256:)
        @fetcher.fetch("https://rubygems.org/gems/#{name}-#{version}.gem", sha256, label(name)) do |source, target|
          package = Gem::Package.new(source)
          package.extract_files(target)
        end

        self
      end

      def ruby_lib(name:, srcs:, deps:[])
        @collector.call RubyLib.new(label: label(name), srcs: srcs, deps: parse(deps))
        self
      end

      def ruby_test(name:, srcs:, deps:[])
        @collector.call RubyTest.new(label: label(name), srcs: srcs, deps: parse(deps))
        self
      end

      private

      def label(name)
        Label.new(nil, @path, name)
      end

      def parse(deps)
        deps.map { |string| Label.parse(string) }
      end
    end



    class KtJvmLib
      attr_reader :label

      def initialize(label:, srcs:)
        @label = label
        @srcs = srcs
      end
    end

    class KtJvmTest
      attr_reader :label

      def initialize(label:, deps:)
        @label = label
        @deps = deps
      end
    end

    class RubyGem
      attr_reader :label
      attr_reader :deps

      def initialize(label:, version:, sha256:)
        @label = label
        @version = version
        @sha256 = sha256
        @deps = [] # TODO hinky that we have to support these now...
      end

      def register(actions)
        # TODO I wonder if I'll need earlier-running "repository rules."
        # For now, it's nice to treat them just the same as all the others.

        # -> var/cache/sha256
        actions.download("https://rubygems.org/gems/#{@label.name}-#{@version}.gem", @sha256)

        # -> var/lib/path/to/package/name
        actions.extract(@sha256) do |source, target|
          package = Gem::Package.new(source)
          package.extract_files(target)
          IO.write("#{target}/gemspec.yaml", package.spec.to_yaml)
        end

        # -> var/tmp/path/to/package/name
        # TODO
        # link,
        # runfiles,
        # ruby_lib provider with require_paths, bubbling up like runfiles
      end
    end

    class RubyLib
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:)
        @label = label
        @srcs = srcs
        @deps = deps
      end

      def register(actions)
        actions.runfiles(
          @srcs.map { |src| actions.link(src) },
          @deps.map { |dep| actions.runfiles_for(dep) }
        )
      end
    end

    class RubyTest
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:[])
        @label = label
        @srcs = srcs
        @deps = deps + [
          Label.parse('//src/main/ruby:wake_testing')
        ]
      end

      def register(actions)
        actions.runfiles(
          @srcs.map { |src| actions.link(src) },
          @deps.map { |dep| actions.runfiles_for(dep) }
        )

        actions.test_executable(
          [
            RbConfig.ruby,
            '-wU',
            '--disable-all',
            '-I', 'src/main/ruby',
            '-r', 'wake/testing'
          ].concat(
            @srcs.flat_map { |src|
              ['-r', File.join('.', @label.package, src)]
            }
          ).concat([
            '-e', "Wake::Testing::Minitest.run(#{@label.name}, STDOUT)"
          ])
        )
      end
    end
  end
end
