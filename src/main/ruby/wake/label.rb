module Wake
  class Label
    def self.parse(string)
      string.match %r{(?:@(\w+))?//(\w+(?:/\w+)*)?:(\w+)} do |match|
        new(match[1], match[2] || '', match[3])
      end
    end

    attr_reader :repository
    attr_reader :package
    attr_reader :name

    def initialize(repository, package, name)
      @repository = repository
      @package = package
      @name = name
    end

    def path(suffix = nil)
      base = File.join(@package, [@name, suffix].compact.join('.'))

      if @repository
        File.join('external', @repository, base)
      else
        base
      end
    end

    def ==(other)
      @package == other.package && @name == other.name
    end

    def eql?(other)
      self == other
    end

    def hash
      to_s.hash
    end

    def to_s
      "//#{@package}:#{@name}"
    end

    def inspect
      to_s
    end
  end
end
