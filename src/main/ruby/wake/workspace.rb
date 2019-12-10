require 'wake/rules'

module Wake
  class Workspace
    def initialize
      builder = Builder.new
      yield builder
      @targets = builder.build
    end

    # deprecated
    def target(label)
      @targets.find { |target| target.label == label } || raise
    end

    def each
      @targets.each do |target|
        yield target
      end
    end

    private

    class Builder
      def initialize
        @targets = []
        @depths = {}
      end

      def load_package(path, contents)
        Rules.load(path, contents) do |target|
          @targets << target
          @depths[target.label] = Depth.new(target.deps)
        end
      end

      def build
        @depths.each_value { |depth| depth.calculate(@depths) }
        @targets.sort_by { |target| [@depths.fetch(target.label), target.label.to_s] }.freeze
      end
    end

    class Depth
      include Comparable

      def initialize(deps)
        @value = deps.empty? ? 0 : -1
        @deps = deps
      end

      def calculate(index)
        if @value == -1
          @value = @deps.map { |dep| index.fetch(dep).calculate(index) }.max.next
        else
          @value
        end
      end

      def <=>(other)
        @value <=> other.instance_variable_get(:@value)
      end
    end
  end
end
