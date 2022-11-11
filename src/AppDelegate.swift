import Cocoa
import os
import src_nest

public class AppDelegate: NSObject, NSApplicationDelegate {
    private let nest: Nest

    public override init() {
        self.nest = Nest(logger: SystemLogger())
    }

    public func applicationDidFinishLaunching(_ aNotification: Notification) {
        nest.evaluate(code: """
            class StatusItem {
                construct new() {
                    System.print("Whee!")
                }
            }
            StatusItem.new()
            """
        )
    }
}

class SystemLogger: NestLogger {
    let logger = Logger()

    func info(_ message: String) {
        logger.info("\(message)")
    }

    func error(_ message: String) {
        logger.error("\(message)")
    }
}
