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

      def http_archive(name:, url:, sha256:, build_file: nil)
        raise unless @path.start_with?('lib/')

        user_cache_home = Pathname.new(ENV.fetch('XDG_CACHE_HOME', File.join(ENV.fetch('HOME'), '.cache'))).join('wake')
        user_cache_home.mkpath
        user_cache = user_cache_home.join(sha256)

        unless user_cache.exist?
          response = Net::HTTP.get_response(URI.parse(url))
          response = Net::HTTP.get_response(URI.parse(response['location'])) if Net::HTTPRedirection === response

          Tempfile.open do |scratch|
            scratch.binmode
            scratch.write(response.body)
            scratch.flush

            if Digest::SHA256.file(scratch.path).hexdigest == sha256
              user_cache.make_link(scratch.path)
            end
          end
        end

        cache = @filesystem.sandbox('var/cache')
        unless cache.exists?(sha256)
          cache.link(sha256, user_cache.to_s)
        end

        label = label(name)
        lib = @filesystem.sandbox('var')
        compressed = cache.absolute_path(sha256)
        extracted = lib.absolute_path(label.path)

        if !File.exist?(extracted) || File.mtime(extracted) < File.mtime(compressed)
          case File.extname(url)
          when '.gem'
            package = Gem::Package.new(compressed)
            package.extract_files(extracted)
            srcs = package.spec.files.select { |path| path.start_with?(package.spec.require_path) }
            load_path = package.spec.require_path
            IO.write("#{extracted}/BUILD", <<~END)
              ruby_lib(
                name: #{name.inspect},
                srcs: #{srcs.inspect},
                load_path: #{load_path.inspect},
              )
            END
          when '.zip'
            FileUtils.mkdir_p(extracted)
            system('unzip', '-q', compressed, '-d', extracted) || fail($?)
          else
            fail 'Unsupported file extension.'
          end

          IO.write("#{extracted}/BUILD", build_file) if build_file
        end

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
        http_archive(name: name, url: "https://rubygems.org/gems/#{name}-#{version}.gem", sha256: sha256)
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
