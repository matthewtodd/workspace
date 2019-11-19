module Wake
  class Label
    def self.parse(string)
      new(*string[2..-1].split(':'))
    end

    attr_reader :repository
    attr_reader :package
    attr_reader :name

    def initialize(package, name)
      @package = package
      @name = name
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

