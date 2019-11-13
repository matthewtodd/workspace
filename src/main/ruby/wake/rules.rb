require 'rbconfig'

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
        Label.new(@path, name)
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

      def accept(visitor)

      end
    end

    class KtJvmTest
      attr_reader :label

      def initialize(label:, deps:)
        @label = label
        @deps = deps
      end

      def accept(visitor)

      end
    end

    class RubyLib
      attr_reader :label

      def initialize(label:, srcs:, deps:)
        @label = label
        @srcs = srcs
        @deps = deps
      end

      def accept(visitor)
        # no-op for now
      end

      def each_runfile(workspace)
        @deps.each do |label|
          workspace.each_runfile(label) do |path|
            yield path
          end
        end

        @srcs.each do |path|
          yield File.join(@label.package, path)
        end
      end
    end

    class RubyTest
      attr_reader :label

      def initialize(label:, srcs:, deps:[])
        @label = label
        @srcs = srcs
        @deps = deps
      end

      def accept(visitor)
        visitor.visit_test(self)
      end

      def each_runfile(workspace)
        @deps.each do |label|
          workspace.each_runfile(label) do |path|
            yield path
          end
        end

        @srcs.each do |path|
          yield File.join(@label.package, path)
        end
      end

      def test_command
        command = [ RbConfig.ruby, '-wU', '--disable-all']
        command += ['-I', 'src/main/ruby']
        command += ['-r', 'wake/testing']
        command += @srcs.flat_map { |src| ['-r', File.join('.', @label.package, src)] }
        command += ['-e', "Wake::Testing::Minitest.run(#{@label.name}, STDOUT)"]
        command
      end
    end
  end
end
