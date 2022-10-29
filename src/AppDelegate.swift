import os
import Cocoa
import Wren

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate {
    private let logger: Logger = Logger()
    private var vm: OpaquePointer?

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        logger.error("applicationDidFinishLaunching")

        var config: WrenConfiguration = WrenConfiguration();

        withUnsafeMutablePointer(to: &config) {
            wrenInitConfiguration($0)
        }

        config.writeFn = { (vm: OpaquePointer?, text: UnsafePointer<CChar>?) -> Void in
            // TODO I think there's some swifty way to say this, maybe with?
            // TODO AppDelegate will get huge if I keep doing this. Pull out a type for the UserData?
            let me: AppDelegate = getUserData(vm: vm)!
            me.logger.warning("\(String(cString: text!))")
        }

        vm = withUnsafeMutablePointer(to: &config) {
            wrenNewVM($0)
        }

        setUserData(vm: vm, userData: self)

        let _ = withUnsafePointer(to: "foo") { (module) in
            withUnsafePointer(to: "System.print(\"Whee!\")") { (source) in
                wrenInterpret(vm, module, source)
            }
        }
    }

    func applicationWillTerminate(_ aNotification: Notification) {
        logger.error("applicationWillTerminate")
        wrenFreeVM(vm)
    }
}

private func getUserData<T: AnyObject>(vm: OpaquePointer?) -> T? {
    return Unmanaged<T>.fromOpaque(wrenGetUserData(vm)).takeUnretainedValue()
}

private func setUserData<T: AnyObject>(vm: OpaquePointer?, userData: T) {
    wrenSetUserData(vm, Unmanaged.passUnretained(userData).toOpaque())
}
