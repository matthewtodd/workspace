import XCTest

import src_nest

class NestTest: XCTestCase {
    func testSystemPrint() {
        var messages = [String]()
        let nest = Nest(logger: { (message: String) in messages.append(message) })
        nest.evaluate(code: "System.print(\"Whee!\")")
        XCTAssertEqual(["Whee!"], messages)
    }
}
