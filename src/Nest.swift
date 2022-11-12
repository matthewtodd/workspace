import Wren

public protocol NestLogger {
    func info(_ message: String)
    func error(_ message: String)
}

struct NullLogger: NestLogger {
    func info(_ message: String) {}
    func error(_ message: String) {}
}

public class Nest {
    let vm: OpaquePointer
    var config = WrenConfiguration()
    let logger: NestLogger
    let modules: Dictionary<String, String>

    public init(logger: NestLogger? = nil, modules: Dictionary<String, String> = Dictionary()) {
        self.logger = logger ?? NullLogger()
        self.modules = modules

        withUnsafeMutablePointer(to: &config) {
            wrenInitConfiguration($0)
        }

        config.writeFn = nestWriteFn
        config.errorFn = nestErrorFn
        config.loadModuleFn = nestLoadModuleFn
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
        wrenInterpret(self.vm, "main", code)
    }
}

func getThis(vm: OpaquePointer?) -> Nest? {
    guard let opaque = wrenGetUserData(vm) else {
        return nil
    }
    return Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()
}

func nestWriteFn(vm: OpaquePointer?, text: UnsafePointer<CChar>?) {
    if let this = getThis(vm: vm) {
        this.logger.info("\(text)")
    }
}

func nestErrorFn(vm: OpaquePointer?, type: WrenErrorType, module: UnsafePointer<CChar>?, line: Int32, message: UnsafePointer<CChar>?) -> Void {
    guard let this = getThis(vm: vm) else {
        return
    }

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

func nestLoadModuleFn(vm: OpaquePointer?, name: UnsafePointer<CChar>?) -> WrenLoadModuleResult {
    var result = WrenLoadModuleResult()

    guard let this = getThis(vm: vm) else {
        return result
    }

    guard let unwrappedName = name else {
        return result
    }

    guard let source = this.modules[String(cString: unwrappedName)] else {
        return result
    }

    source.utf8CString.withUnsafeBytes {
        // TODO free this memory - pass length to userData, freeing callback to onComplete?
        let copy = UnsafeMutableRawBufferPointer.allocate(byteCount: $0.count, alignment: MemoryLayout<CChar>.alignment)
        copy.copyMemory(from: $0)
        result.source = UnsafeBufferPointer(copy.bindMemory(to: CChar.self)).baseAddress
    }

    return result
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
