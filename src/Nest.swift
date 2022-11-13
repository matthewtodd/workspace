import Wren

public protocol NestLogger {
    func info(_ message: String)
    func error(_ message: String)
}

struct NullLogger: NestLogger {
    func info(_ message: String) {}
    func error(_ message: String) {}
}

public struct NestModule {
    let source: String

    public init(source: String) {
        self.source = source
    }
}

// TODO I think config and VM are going to separate here?
public class Nest {
    private let vm: OpaquePointer
    private var config = WrenConfiguration()
    private let logger: NestLogger
    private let modules: Dictionary<String, NestModule>

    public init(logger: NestLogger? = nil, modules: Dictionary<String, NestModule> = Dictionary()) {
        self.logger = logger ?? NullLogger()
        self.modules = modules

        // TODO do I need this? Is init() enough?
        withUnsafeMutablePointer(to: &config) {
            wrenInitConfiguration($0)
        }

        config.writeFn = nestWrite
        config.errorFn = nestError
        config.loadModuleFn = nestLoadModuleFn
        config.bindForeignMethodFn = nestBindForeignMethodFn
        // TODO pass more config here

        self.vm = withUnsafeMutablePointer(to: &config) {
            wrenNewVM($0)
        }

        wrenSetUserData(vm, Unmanaged.passUnretained(self).toOpaque())
    }

    deinit {
        wrenFreeVM(vm)
    }

    public func evaluate(source: String) {
        // TODO default module name?
        wrenInterpret(vm, "main", source)
    }

    func write(text: String) {
        logger.info(text)
    }

    func error(type: WrenErrorType, module: String?, line: Int32, message: String) {
        switch type {
        case WREN_ERROR_COMPILE:
            logger.error("[\(module!) line \(line)] [Error] \(message)")
            break;
        case WREN_ERROR_STACK_TRACE:
            logger.error("[\(module!) line \(line)] in \(message)")
            break;
        case WREN_ERROR_RUNTIME:
            logger.error("[Runtime Error] \(message)")
            break;
        default:
            break;
        }
    }

    func loadModule(name: String) -> WrenLoadModuleResult {
        guard let module = modules[name] else { return WrenLoadModuleResult() }

        var source: UnsafePointer<CChar>?
        let onComplete: WrenLoadModuleCompleteFn? = Optional.none
        let userData: UnsafeMutableRawPointer? = Optional.none

        module.source.utf8CString.withUnsafeBytes {
            // TODO free this memory - pass length to userData, freeing callback to onComplete?
            let copy = UnsafeMutableRawBufferPointer.allocate(byteCount: $0.count, alignment: MemoryLayout<CChar>.alignment)
            copy.copyMemory(from: $0)
            source = UnsafeBufferPointer(copy.bindMemory(to: CChar.self)).baseAddress
        }

        return WrenLoadModuleResult(
            source: source,
            onComplete: onComplete,
            userData: userData
        )
    }
}

func getThis(vm: OpaquePointer?) -> Nest? {
    guard let opaque = wrenGetUserData(vm) else { return nil }
    return Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()
}

func nestWrite(vm: OpaquePointer?, text: UnsafePointer<CChar>?) {
    guard let this = getThis(vm: vm) else { return }
    guard let unwrappedText = text else { return }
    this.write(text: String(cString: unwrappedText))
}

func nestError(vm: OpaquePointer?, type: WrenErrorType, module: UnsafePointer<CChar>?, line: Int32, message: UnsafePointer<CChar>?) -> Void {
    guard let this = getThis(vm: vm) else { return }
    guard let unwrappedMessage = message else { return }
    this.error(type: type, module: module.map { String(cString: $0) }, line: line, message: String(cString: unwrappedMessage))
}

func nestLoadModuleFn(vm: OpaquePointer?, name: UnsafePointer<CChar>?) -> WrenLoadModuleResult {
    guard let this = getThis(vm: vm) else { return WrenLoadModuleResult() }
    guard let unwrappedName = name else { return WrenLoadModuleResult() }
    return this.loadModule(name: String(cString: unwrappedName))
}

func nestBindForeignMethodFn(vm: OpaquePointer?, module: UnsafePointer<CChar>?, className: UnsafePointer<CChar>?, isStatic: Bool, signature: UnsafePointer<CChar>?) -> WrenForeignMethodFn? {
    // TODO lift this code up into the test, as NestModule behavior
    // It's unclear whether there's value in insulating NestModule from Wren.
    return { (vm: OpaquePointer?) in
        "bar".utf8CString.withUnsafeBytes {
            wrenSetSlotString(vm!, 0, $0.baseAddress)
        }
    }
}
