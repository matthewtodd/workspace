require 'net/http'
require 'pathname'
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

      def http_archive(name:, url:, sha256:, &rules)
        raise unless @path.start_with?('lib/')

        cache = @filesystem.sandbox('var/cache')

        unless cache.exists?(sha256)
          response = Net::HTTP.get_response(URI.parse(url))
          response = Net::HTTP.get_response(URI.parse(response['location'])) if Net::HTTPRedirection === response

          Tempfile.open do |scratch|
            scratch.binmode
            scratch.write(response.body)
            scratch.flush

            if Digest::SHA256.file(scratch.path).hexdigest == sha256
              cache.link(sha256, scratch.path)
            end
          end
        end

        label = label(name)
        lib = @filesystem.sandbox('var')
        compressed = cache.absolute_path(sha256)
        extracted = lib.absolute_path(label.path)
        extractor = case File.extname(url)
          when '.gem'
            lambda do |compressed, extracted|
              package = Gem::Package.new(compressed)
              package.extract_files(extracted)
              # I considered writing a BUILD file here, but on balance maybe (at
              # least while I'm developing here) it's nice to not have to re-run
              # the extraction when I change something.
              IO.write("#{extracted}/gemspec.yaml", package.spec.to_yaml)
            end
          when '.zip'
            lambda do |compressed, extracted|
              FileUtils.mkdir_p(extracted)
              system('unzip', '-q', compressed, '-d', extracted) || fail($?)
            end
          else
            fail 'Unsupported file extension.'
          end

        if !File.exist?(extracted) || File.mtime(extracted) < File.mtime(compressed)
          extractor.call(compressed, extracted)
        end

        rules.call(extracted)

        self
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
        http_archive(name: name, url: "https://rubygems.org/gems/#{name}-#{version}.gem", sha256: sha256) do |extracted|
          gemspec = YAML.load_file("#{extracted}/gemspec.yaml")

          files_on_the_load_path = gemspec.files.select { |path|
            path.start_with?(gemspec.require_path)
          }

          ruby_lib(
            name: name,
            srcs: files_on_the_load_path.map { |path| File.join(name, path) },
            load_path: File.join(name, gemspec.require_path),
          )
        end

        self
      end

      def ruby_lib(name:, srcs:, deps:[], load_path: '.')
        @collector.call RubyLib.new(label: label(name), srcs: srcs, deps: parse(deps), load_path: canonical_load_path(load_path))
        self
      end

      def ruby_test(name:, srcs:, deps:[])
        @collector.call RubyTest.new(label: label(name), srcs: srcs, deps: parse(deps))
        self
      end

      private

      def label(name)
        @path == '.' ? Label.new('', name) : Label.new(@path, name)
      end

      def parse(deps)
        deps.map { |string| Label.parse(string) }
      end

      def canonical_load_path(load_path)
        Pathname.new(@path).join(load_path).cleanpath.to_s
      end
    end

    class KtJvmLib
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:)
        @label = label
        @srcs = srcs
        @deps = []
      end

      def register(actions)

      end
    end

    class KtJvmTest
      attr_reader :label
      attr_reader :deps

      def initialize(label:, deps:)
        @label = label
        @deps = deps
      end

      def register(actions)

      end
    end

    class RubyLib
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:, load_path:)
        @label = label
        @srcs = srcs
        @deps = deps
        @load_path = load_path
      end

      def register(actions)
        actions.info(:ruby_load_path,
          @load_path,
          @deps.map { |dep| actions.info_for(dep, :ruby_load_path) }
        )

        actions.info(:runfiles,
          @srcs.map { |src| actions.link(src) },
          @deps.map { |dep| actions.info_for(dep, :runfiles) }
        )
      end
    end

    class RubyTest
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:[])
        @label = label
        @srcs = srcs
        @deps = deps

        fail unless @srcs.length == 1
      end

      def register(actions)
        actions.info(:runfiles,
          @srcs.map { |src| actions.link(src) },
          @deps.map { |dep| actions.info_for(dep, :runfiles) }
        )

        load_paths = @deps.map { |dep| actions.info_for(dep, :ruby_load_path) }.flatten.sort.uniq

        actions.test_executable(
          [
            RbConfig.ruby,
            '-wU',
            '--disable-all',
          ].concat(
            load_paths.flat_map { |path| ['-I', path] }
          ).concat(
            @srcs.map { |src| @label.path_to(src) }
          )
        )
      end
    end
  end
end
