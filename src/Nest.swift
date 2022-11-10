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
    this.logger.info(String(cString: text!))
}

func nestErrorFn(vm: OpaquePointer?, type: WrenErrorType, module: UnsafePointer<CChar>?, line: Int32, message: UnsafePointer<CChar>?) -> Void {
    let opaque = wrenGetUserData(vm)!
    let this = Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()

    switch type {
    case WREN_ERROR_COMPILE:
        // TODO: look up string formatting when online. use varargs in logger.
        // printf("[%s line %d] [Error] %s\n", module, line, msg);
        this.logger.error(String(cString: message!))
        break;
    case WREN_ERROR_STACK_TRACE:
        // TODO: look up string formatting when online. use varargs in logger.
        // printf("[%s line %d] in %s\n", module, line, msg);
        this.logger.error(String(cString: message!))
        break;
    case WREN_ERROR_RUNTIME:
        // TODO: look up string formatting when online. use varargs in logger.
        // printf("[Runtime Error] %s\n", msg);
        this.logger.error(String(cString: message!))
        break;
    default:
        break;
    }
}
