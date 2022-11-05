public class Nest {
    let logger: (String) -> Void

    public init(logger: @escaping (String) -> Void) {
        self.logger = logger
    }

    public func evaluate(code: String) {
        self.logger("Whee!")
    }
}
