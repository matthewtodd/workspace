package org.matthewtodd.wake.test

import kotlin.native.internal.test.TestSuite

class NativeTestRunner(val suites: List<TestSuite>) {
  private val runner = TestRunner(NativeBacktraceInterpreter())

  fun run() {
    suites.flatMap { it.testCases.values }.forEach {
      runner.test(
        it.suite.name,
        it.name,
        it.ignored,
        it::run
      )
    }
  }
}

class NativeBacktraceInterpreter : BacktraceInterpreter {
  override fun errorType(e: Throwable) =
    e.toString().split(":").first()

  override fun errorBacktrace(e: Throwable) =
    e.getStackTrace().asSequence().dropWhile(::shouldFilter).toList()

  private fun shouldFilter(line: String): Boolean {
    return listOf(
      "Error#<init>",
      "Exception#<init>",
      "Throwable#<init>",
      "kotlin.test#assert",
      "kotlin.test.Asserter",
      "kotlin.test.DefaultAsserter",
    ).any(line::contains)
  }
}
