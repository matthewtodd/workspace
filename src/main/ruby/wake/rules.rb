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

      def http_archive(name:, url:, sha256:, strip_components: 0, includes: [], build_file:)
        raise("http_archive only works inside lib [#{@path.inspect}]") unless @path.split('/').first == 'lib'

        archive = fetch(url, sha256: sha256)

        producing(subpackage(name), from: archive) do |extracted|
          if url.end_with?('.tar.gz')
            Dir.mktmpdir do |tmp|
              system('tar', 'xzf', archive, '--directory', tmp) || fail($?.to_s)
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
              system('unzip', '-q', archive, '-d', tmp) || fail($?.to_s)
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

      def java_import(name:, jar:)
        label = label(name)
        jar = label.path_to(jar)
        @collector.call JavaImport.new(label: label, jar: jar)
        self
      end

      def java_lib(name:, srcs:, deps:[], javac:)
        label = label(name)
        srcs = srcs.map { |src| label.path_to(src) }
        deps = parse(deps)
        javac = Label.parse(javac)
        @collector.call JavaLib.new(label: label, srcs: srcs, deps: deps, javac: javac)
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
        raise("maven_jar only works inside lib [#{@path.inspect}]") unless @path.split('/').first == 'lib'

        group_id, artifact_id, version = artifact.split(':')
        jar = fetch("https://repo1.maven.org/maven2/#{group_id.gsub('.', '/')}/#{artifact_id}/#{version}/#{artifact_id}-#{version}.jar", sha256: sha256)

        producing(subpackage(group_id.gsub('.', '_'), artifact_id.gsub('-', '_')), from: jar) do |extracted|
          extracted.link("#{artifact_id}.jar", jar)

          IO.write(extracted.absolute_path('BUILD'), <<~END)
            java_import(
              name: 'jar',
              jar: '#{artifact_id}.jar',
            )
          END
        end
      end

      def ruby_gem(name:, version:, sha256:)
        raise("ruby_gem only works inside lib [#{@path.inspect}]") unless @path.split('/').first == 'lib'

        archive = fetch("https://rubygems.org/gems/#{name}-#{version}.gem", sha256: sha256)

        producing(subpackage(name), from: archive) do |extracted|
          package = Gem::Package.new(archive)
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
        label = label(name)
        srcs = srcs.map { |src| label.path_to(src) }
        deps = parse(deps)
        load_path = canonical_load_path(load_path)
        @collector.call RubyLib.new(label: label, srcs: srcs, deps: deps, load_path: load_path)
        self
      end

      def ruby_test(name:, srcs:, deps:[])
        label = label(name)
        srcs = srcs.map { |src| label.path_to(src) }
        deps = parse(deps)
        @collector.call RubyTest.new(label: label, srcs: srcs, deps: deps)
        self
      end

      private

      def fetch(url, sha256:)
        @filesystem.sandbox('var/cache')
          .link(sha256, fetch_into_user_cache(url, sha256: sha256))
          .absolute_path(sha256)
      end

      def fetch_into_user_cache(url, sha256:)
        user_cache_home = Pathname.new(ENV.fetch('XDG_CACHE_HOME', File.join(ENV.fetch('HOME'), '.cache'))).join('wake')
        user_cache_home.mkpath
        user_cache = user_cache_home.join(sha256)

        unless user_cache.exist?
          Net::HTTP.get_response(URI.parse(url)) do |response|
            if Net::HTTPRedirection === response
              fetch_into_user_cache(response['location'], sha256: sha256)
            else
              Tempfile.open do |scratch|
                scratch.binmode
                response.read_body { |chunk| scratch.write(chunk) }
                scratch.flush

                if Digest::SHA256.file(scratch.path).hexdigest == sha256
                  user_cache.make_link(scratch.path)
                end
              end
            end
          end
        end

        user_cache.to_s
      end

      def label(name)
        @path == '.' ? Label.new('', name) : Label.new(@path, name)
      end

      def subpackage(*components)
        @path == '.' ? File.join(*components) : File.join(@path, *components)
      end

      def parse(deps)
        deps.map { |string| Label.parse(string) }
      end

      def producing(path, from:, &extractor)
        lib = @filesystem.sandbox('var')
        extracted = lib.sandbox(path)
        if !extracted.exists? || extracted.mtime < File.mtime(from)
          extractor.call(extracted)
        end
      end

      def canonical_load_path(load_path)
        Pathname.new(@path).join(load_path).cleanpath.to_s
      end
    end

    class JavaImport
      attr_reader :label
      attr_reader :deps

      def initialize(label:, jar:)
        @label = label
        @jar = jar
        @deps = [] # hinky
      end

      def register(actions)
        link = actions.link(@jar)
        actions.info(:java_classpath, link, [])
        actions.info(:runfiles, link, [])
      end
    end

    class JavaLib
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:, javac:)
        @label = label
        @srcs = srcs
        @deps = deps
        @javac = javac
      end

      def register(actions)
        # inputs = []
        # output = label.path('jar')

        # jar = actions.run(inputs:inputs, output:output) do |execroot, tmpdir|

        # end

        # actions.info(:java_classpath,
        #   jar,
        #   @deps.map { |dep| actions.info_for(dep, :java_classpath) }
        # )

        # actions.info(:runfiles,
        #   jar,
        #   @deps.map { |dep| actions.info_for(dep, :runfiles) }
        # )
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
        # outputs = actions.run(inputs: @srcs, outputs: @srcs) do |execroot, tmpdir|
        #   @srcs.map { |src| [
        #     FileUtils.join(execroot, src),
        #     FileUtils.join(tmpdir, src)
        #   ]}.each { |src, dest|
        #     FileUtils.mkdir_p(File.dirname(dest))
        #     FileUtils.ln(src, dest)
        #   }
        # end

        actions.info(:ruby_load_path,
          @load_path,
          @deps.map { |dep| actions.info_for(dep, :ruby_load_path) }
        )

        actions.info(:runfiles,
          # outputs,
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
            @srcs
          )
        )
      end
    end
  end
end
