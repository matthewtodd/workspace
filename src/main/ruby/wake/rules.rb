require 'net/http'
require 'rbconfig'
require 'rubygems'
require 'rubygems/package'
require 'tempfile'
require 'wake/label'
require 'yaml'

module Wake
  class Rules
    def initialize(filesystem)
      @filesystem = filesystem
    end

    def load(path, contents, &collector)
      Dsl.new(path, @filesystem, collector).instance_eval(contents)
    end

    class Dsl
      def initialize(path, filesystem, collector)
        @path = path
        @filesystem = filesystem
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
        # TODO extract an object or two out of here, once I can get something
        # working. I tried at first, but the boundaries were more than I could
        # reason about at the time.

        raise unless @path.start_with?('lib/')

        cache = @filesystem.sandbox('var/cache')

        unless cache.exists?(sha256)
          Net::HTTP.get_response(URI.parse("https://rubygems.org/gems/#{name}-#{version}.gem")) do |response|
            Tempfile.open do |scratch|
              scratch.binmode
              response.read_body { |segment| scratch.write(segment) }
              scratch.flush

              if Digest::SHA256.file(scratch.path).hexdigest == sha256
                cache.link(sha256, scratch.path)
              end
            end
          end
        end

        label = label(name)
        lib = @filesystem.sandbox('var')
        compressed = cache.absolute_path(sha256)
        extracted = lib.absolute_path(label.path)

        if !File.exist?(extracted) || File.mtime(extracted) < File.mtime(compressed)
          package = Gem::Package.new(compressed)
          package.extract_files(extracted)
          # I considered writing a BUILD file here, but on balance maybe (at
          # least while I'm developing here) it's nice to not have to re-run
          # the extraction when I change something.
          IO.write("#{extracted}/gemspec.yaml", package.spec.to_yaml)
        end

        # gemspec = YAML.load_file("#{extracted}/gemspec.yaml")

        # Need at least two things here:
        #
        # 1. Add a "minitest" somewhere.
        #    As it is, we have //lib/rubygems:minitest for our label and we are trying to link
        #    /Users/matthew/workspace/lib/rubygems/lib/hoe/minitest.rb,
        #    /Users/matthew/workspace/var/tmp/lib/rubygems/lib/hoe/minitest.rb
        #
        # 2. Pass a separate filesystem sandbox to actions.link so that it
        #    looks in var for the source. Some modeling might emerge.
        #
        # ruby_lib(
        #   name: name,
        #   srcs: gemspec.files.select { |path| gemspec.require_paths.any? { |require_path| path.start_with?(require_path) } },
        # )

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
