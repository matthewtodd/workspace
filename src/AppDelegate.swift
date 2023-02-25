import Cocoa
import Wren
import os
import src_nest

public class AppDelegate: NSObject, NSApplicationDelegate {
    private let nest: Nest

    public override init() {
        self.nest = Nest(logger: SystemLogger(), modules: [
            "status_item": NestModule(
                source: """
                    foreign class StatusItem {
                        construct new() {
                            System.print("Whee!")
                        }
                    }
                """,
                foreignClasses: [
                    "StatusItem": WrenForeignClassMethods(
                        allocate: { (vm: OpaquePointer?) in
                            var item = NSStatusBar.system.statusItem(withLength: NSStatusItem.variableLength)

                            withUnsafeRawPointer(to: &item) {
                                wrenSetSlotNewForeign(vm!, 0, 0, $0.cou)
                            }
                        },
                        finalize: { (data: UnsafeMutableRawPointer?) in

                        }
                    )
                ]
            )
        ])
    }

    public func applicationDidFinishLaunching(_ aNotification: Notification) {
        nest.evaluate(source: """
            import "status_item" for StatusItem

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
