module Wake
  class Label
    def self.parse(string)
      string.match %r{//(\w+(?:/\w+)*)?:(\w+)} do |match|
        new(match[1] || '', match[2])
      end
    end

    def initialize(package, name)
      @package = package
      @name = name
    end

    def path(suffix = nil)
      path_to [@name, suffix].compact.join('.')
    end

    def path_to(path)
      @package.empty? ? path : File.join(@package, path)
    end

    def eql?(other)
      to_s == other.to_s
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
