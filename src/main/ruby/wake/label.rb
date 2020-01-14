module Wake
  class Label
    def self.parse(string)
      string.match %r{//(\w+(?:/\w+)*)?:(\w+)} do |match|
        new(match[1] || '', match[2])
      end
    end

    attr_reader :package
    attr_reader :name

    def initialize(package, name)
      @package = package
      @name = name
    end

    def path(suffix = nil)
      File.join(@package, [@name, suffix].compact.join('.'))
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
