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
        @fetcher = Fetcher.new(@filesystem.sandbox('var/cache'))
      end

      class Fetcher
        def initialize(cache)
          @cache = cache
        end

        def fetch(url:, sha256:)
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

          unless @cache.exists?(sha256)
            @cache.link(sha256, user_cache.to_s)
          end

          @cache.absolute_path(sha256)
        end
      end

      def http_archive(name:, url:, sha256:, strip_components: 0, includes: [], build_file:)
        raise("http_archive only works inside lib [#{@path.inspect}]") unless @path.split('/').first == 'lib'

        label = label(name)
        lib = @filesystem.sandbox('var')
        compressed = @fetcher.fetch(url: url, sha256: sha256)
        extracted = lib.sandbox(label.path)

        if !extracted.exists? || extracted.mtime < File.mtime(compressed)
          if url.end_with?('.tar.gz')
            Dir.mktmpdir do |tmp|
              system('tar', 'xzf', compressed, '--directory', tmp) || fail($?.to_s)
              includes.each do |pattern|
                Dir.glob(pattern, base: tmp) do |path|
                  src = File.join(tmp, path)
                  if File.file?(src)
                    dest = path.split('/', strip_components.next).last
                    extracted.link(dest, src)
                  end
                end
              end
            end
          elsif url.end_with?('.zip')
            Dir.mktmpdir do |tmp|
              system('unzip', '-q', compressed, '-d', tmp) || fail($?.to_s)
              includes.each do |pattern|
                Dir.glob(pattern, base: tmp) do |path|
                  src = File.join(tmp, path)
                  if File.file?(src)
                    dest = path.split('/', strip_components.next).last
                    extracted.link(dest, src)
                  end
                end
              end
            end
          else
            fail "Unsupported file extension for #{File.basename(url)}"
          end

          IO.write(extracted.absolute_path('BUILD'), build_file)
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

      def maven_jar(artifact:, sha256:)

      end

      def ruby_gem(name:, version:, sha256:)
        raise("ruby_gem only works inside lib [#{@path.inspect}]") unless @path.split('/').first == 'lib'

        label = label(name)
        lib = @filesystem.sandbox('var')
        compressed = @fetcher.fetch(url: "https://rubygems.org/gems/#{name}-#{version}.gem", sha256: sha256)
        extracted = lib.sandbox(label.path)

        if !extracted.exists? || extracted.mtime < File.mtime(compressed)
          package = Gem::Package.new(compressed)
          package.extract_files(extracted.mkpath)
          srcs = package.spec.files.select { |path| path.start_with?(package.spec.require_path) }
          load_path = package.spec.require_path
          IO.write(extracted.absolute_path('BUILD'), <<~END)
            ruby_lib(
              name: #{name.inspect},
              srcs: #{srcs.inspect},
              load_path: #{load_path.inspect},
            )
          END
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
