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
      @targets.fetch(label)
    end

    def each
      # can this be as simple as sorting the targets first?
      # i.e., I don't need a real graph if this part happens serially.
      # what do I sort on?
      # actually, sorting doesn't work, since there's no pairwise answer.
      # so? what to do? actually build a graph, then serialize it?
      @targets.each do |label, target|
        yield label, target
      end
    end

    private

    class Builder
      def initialize
        @targets = {}
      end

      def load_package(path, contents)
        Rules.load(path, contents) { |target| @targets[target.label] = target }
      end

      def build
        @targets.freeze
      end
    end
  end
end
