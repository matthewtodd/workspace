import Wren
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

    func testForeignStaticMethod() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger, modules: [
            "foo": NestModule(
                source: """
                    class Foo {
                        foreign static bar()
                    }
                """,
                foreignMethods: [
                    NestForeignMethodKey(className: "Foo", isStatic: true, signature: "bar()"): { (vm: OpaquePointer?) in
                        "bar".utf8CString.withUnsafeBytes {
                            wrenSetSlotString(vm!, 0, $0.baseAddress)
                        }
                    },
                ]
            )
        ])
        nest.evaluate(source: """
            import "foo" for Foo
            System.print(Foo.bar())
        """)
        XCTAssertEqual([], logger.error)
        XCTAssertEqual(["bar", "\n"], logger.info)
    }

    func testForeignClass() {
        let logger = FakeLogger()
        let nest = Nest(logger: logger, modules: [
            "foo": NestModule(
                source: """
                    foreign class Foo {
                        construct new() {}
                        foreign bar()
                    }
                """,
                foreignClasses: [
                    "Foo": WrenForeignClassMethods(
                        allocate: { (vm: OpaquePointer?) in
                            "bar".utf8CString.withUnsafeBytes {
                                let data = wrenSetSlotNewForeign(vm!, 0, 0, $0.count)
                                data?.copyMemory(from: $0.baseAddress!, byteCount: $0.count)
                            }

                        },
                        finalize: { (data: UnsafeMutableRawPointer?) in
                            // Nothing needed here? "bar" above will have been collected, wren's data will be, too.
                        }
                    )
                ],
                foreignMethods: [
                    NestForeignMethodKey(className: "Foo", isStatic: false, signature: "bar()"): { (vm: OpaquePointer?) in
                        wrenSetSlotString(vm!, 0, wrenGetSlotForeign(vm!, 0))
                    },
                ]
            )
        ])
        nest.evaluate(source: """
            import "foo" for Foo
            System.print(Foo.new().bar())
        """)
        XCTAssertEqual([], logger.error)
        XCTAssertEqual(["bar", "\n"], logger.info)
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
