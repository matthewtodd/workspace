import XCTest

import src_nest

class NestTest: XCTestCase {
    func testSystemPrint() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(code: "System.print(\"Whee!\")")
        XCTAssertEqual(["Whee!", "\n"], logger.info)
    }

    func testCompileError() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(code: "\"unterminated string literal")
        XCTAssertEqual(["[main line 1] [Error] Error: Unterminated string."], logger.error)
    }

    func testRuntimeError() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger)
        nest.evaluate(code: "Fiber.abort(\"Boom!\")")
        XCTAssertEqual(["[Runtime Error] Boom!", "[main line 1] in (script)"], logger.error)
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
