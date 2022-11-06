import Wren

public class Nest {
    let vm: OpaquePointer
    var config = WrenConfiguration()
    let logger: (String) -> Void

    public init(logger: @escaping (String) -> Void) {
        self.logger = logger

        withUnsafeMutablePointer(to: &config) {
            wrenInitConfiguration($0)
        }

        config.writeFn = nestWriteFn
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
    let nest = Unmanaged<Nest>.fromOpaque(opaque).takeUnretainedValue()
    nest.logger(String(cString: text!))
}
