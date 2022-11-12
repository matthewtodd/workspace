import XCTest

import src_nest

class NestTest: XCTestCase {
    func testSystemPrint() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(source: #"System.print("Whee!")"#)
        XCTAssertEqual(["Whee!", "\n"], logger.info)
    }

    func testCompileError() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(source: #""unterminated string literal"#)
        XCTAssertEqual(["[main line 1] [Error] Error: Unterminated string."], logger.error)
    }

    func testRuntimeError() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(source: #"Fiber.abort("Boom!")"#)
        XCTAssertEqual(["[Runtime Error] Boom!", "[main line 1] in (script)"], logger.error)
    }

    func testImport() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger, modules: [
            "widget": NestModule(
                source: """
                    class Widget {
                        construct new() {}
                    }
                """
            )
        ])
        nest.evaluate(source: """
            import "widget" for Widget
            System.print(Widget.new())
        """)
        XCTAssertEqual([], logger.error)
        XCTAssertEqual(["instance of Widget", "\n"], logger.info)
    }
}

class FakeLogger: NestLogger {
    var info: [String] = [String]()
    var error: [String] = [String]()

    func info(_ message: String) {
        info.append(message)
    }

    func error(_ message: String) {
        error.append(message)
    }
}
