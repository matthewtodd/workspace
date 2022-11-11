import Wren

public protocol NestLogger {
    func info(_ message: String)
    func error(_ message: String)
}

public class Nest {
    let vm: OpaquePointer
    var config = WrenConfiguration()
    let logger: NestLogger

    public init(logger: NestLogger) {
        self.logger = logger

        withUnsafeMutablePointer(to: &config) {
            wrenInitConfiguration($0)
        }

        config.writeFn = nestWriteFn
        config.errorFn = nestErrorFn
        // TODO pass more config here

        self.vm = withUnsafeMutablePointer(to: &config) {
            wrenNewVM($0)
        }

        wrenSetUserData(vm, Unmanaged.passUnretained(self).toOpaque())
    }

    deinit {
        wrenFreeVM(vm)
    }

    public func evaluate(code: String) {
        // TODO default module name?
        wrenInterpret(self.vm, "foo", code)
    }
}

func nestWriteFn(vm: OpaquePointer?, text: UnsafePointer<CChar>?) {
    let opaque = wrenGetUserData(vm)!
    let this = Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()
    this.logger.info("\(text)")
}

func nestErrorFn(vm: OpaquePointer?, type: WrenErrorType, module: UnsafePointer<CChar>?, line: Int32, message: UnsafePointer<CChar>?) -> Void {
    let opaque = wrenGetUserData(vm)!
    let this = Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()

    switch type {
    case WREN_ERROR_COMPILE:
        this.logger.error("[\(module) line \(line)] [Error] \(message)")
        break;
    case WREN_ERROR_STACK_TRACE:
        this.logger.error("[\(module) line \(line)] in \(message)")
        break;
    case WREN_ERROR_RUNTIME:
        this.logger.error("[Runtime Error] \(message)")
        break;
    default:
        break;
    }
}

/// This StringInterpolation extension hides a lot of the line noise of logging
/// the C strings we get back from the Wren API. By marking it private, we
/// ensure that it will only affect this file.
///
/// SeeAlso: [Super-powered string interpolation in Swift 5.0](https://www.hackingwithswift.com/articles/178/super-powered-string-interpolation-in-swift-5-0)
private extension String.StringInterpolation {
    mutating func appendInterpolation(_ value: UnsafePointer<CChar>?) {
        if let unwrapped = value {
            appendLiteral(String(cString: unwrapped))
        }
    }
}
