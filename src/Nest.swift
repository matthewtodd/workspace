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
    private let source: String

    public init(source: String) {
        self.source = source
    }

    func load() -> WrenLoadModuleResult {
        var source: UnsafePointer<CChar>?
        let onComplete: WrenLoadModuleCompleteFn? = Optional.none
        let userData: UnsafeMutableRawPointer? = Optional.none

        self.source.utf8CString.withUnsafeBytes {
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

    func bindForeignMethod(className: String, isStatic: Bool, signature: String) -> WrenForeignMethodFn? {
        // TODO lift this code up into the test, as NestModule behavior
        // It's unclear whether there's value in insulating NestModule from Wren.
        return { (vm: OpaquePointer?) in
            "bar".utf8CString.withUnsafeBytes {
                wrenSetSlotString(vm!, 0, $0.baseAddress)
            }
        }
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
        config.loadModuleFn = nestLoadModule
        config.bindForeignMethodFn = nestBindForeignMethod
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
        modules[name].map { $0.load() } ?? WrenLoadModuleResult()
    }

    func bindForeignMethod(module: String, className: String, isStatic: Bool, signature: String) -> WrenForeignMethodFn? {
        return modules[module].flatMap { $0.bindForeignMethod(className: className, isStatic: isStatic, signature: signature) }
    }
}

func nest(vm: OpaquePointer?) -> Nest? {
    return vm.flatMap(wrenGetUserData)
        .flatMap { Unmanaged<Nest>.fromOpaque($0) }
        .flatMap { $0.takeUnretainedValue() }
}

func nestWrite(vm: OpaquePointer?, text: UnsafePointer<CChar>?) {
    guard let nest = nest(vm: vm) else { return }
    guard let unwrappedText = text else { return }
    nest.write(text: String(cString: unwrappedText))
}

func nestError(vm: OpaquePointer?, type: WrenErrorType, module: UnsafePointer<CChar>?, line: Int32, message: UnsafePointer<CChar>?) -> Void {
    guard let nest = nest(vm: vm) else { return }
    guard let unwrappedMessage = message else { return }
    nest.error(type: type, module: module.map { String(cString: $0) }, line: line, message: String(cString: unwrappedMessage))
}

func nestLoadModule(vm: OpaquePointer?, name: UnsafePointer<CChar>?) -> WrenLoadModuleResult {
    guard let nest = nest(vm: vm) else { return WrenLoadModuleResult() }
    guard let unwrappedName = name else { return WrenLoadModuleResult() }
    return nest.loadModule(name: String(cString: unwrappedName))
}

func nestBindForeignMethod(vm: OpaquePointer?, module: UnsafePointer<CChar>?, className: UnsafePointer<CChar>?, isStatic: Bool, signature: UnsafePointer<CChar>?) -> WrenForeignMethodFn? {
    guard let nest = nest(vm: vm) else { return nil }
    guard let unwrappedModule = module else { return nil }
    guard let unwrappedClassName = className else { return nil }
    guard let unwrappedSignature = signature else { return nil }
    return nest.bindForeignMethod(module: String(cString: unwrappedModule), className: String(cString: unwrappedClassName), isStatic: isStatic, signature: String(cString: unwrappedSignature))
}
