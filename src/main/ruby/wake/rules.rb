require 'rbconfig'
require 'wake/label'

module Wake
  module Rules
    def self.load(path, contents, &collector)
      Dsl.new(path, collector).instance_eval(contents)
    end

    class Dsl
      def initialize(path, collector)
        @path = path
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

    class RubyLib
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:)
        @label = label
        @srcs = srcs
        @deps = deps
      end

      def register(actions)
        outputs = @srcs.map { |src| actions.link(src) }
        actions.runfiles(outputs, @deps.map { |dep| actions.runfiles_for(dep) })
      end
    end

    class RubyTest
      attr_reader :label
      attr_reader :deps

      def initialize(label:, srcs:, deps:[])
        @label = label
        @srcs = srcs
        @deps = deps
      end

      def register(actions)
        outputs = @srcs.map { |src| actions.link(src) }
        # TODO get back to repositories: I implicitly depend on @wake//:testing.
        outputs << actions.hardcoded_link(Wake::Testing.source_location, 'src/main/ruby/wake/testing.rb')
        runfiles = actions.runfiles(outputs, @deps.map { |dep| actions.runfiles_for(dep) })
        actions.test_executable(test_command, runfiles)
      end

      private

      def test_command
        command = [ RbConfig.ruby, '-wU', '--disable-all']
        command += ['-I', 'src/main/ruby'] # TODO uniq load paths of all deps, plus me?
        command += ['-r', 'wake/testing']
        command += @srcs.flat_map { |src| ['-r', File.join('.', @label.package, src)] }
        command += ['-e', "Wake::Testing::Minitest.run(#{@label.name}, STDOUT)"]
        command
      end
    end
  end
end
