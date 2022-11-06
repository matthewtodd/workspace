import Cocoa
import os
import src_nest

public class AppDelegate: NSObject, NSApplicationDelegate {
    private let nest: Nest

    public override init() {
        let logger = Logger()
        self.nest = Nest(logger: { (message: String) in logger.info("\(message)") })
    }

    public func applicationDidFinishLaunching(_ aNotification: Notification) {
        nest.evaluate(code: "System.print(\"WE DID IT!\")")
    }
}
