package org.matthewtodd.wake.test

import kotlin.native.internal.test.TestSuite

class NativeTestRunner(val suites: List<TestSuite>) {
  private val runner = TestRunner()

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

actual fun errorType(e: Throwable) = e.toString().split(":").first()

actual fun errorBacktrace(e: Throwable) = e.getStackTrace().asSequence().dropWhile(::shouldFilter).toList()

fun shouldFilter(@Suppress("UNUSED_PARAMETER") line: String): Boolean {
  return listOf(
    "Error#<init>",
    "Exception#<init>",
    "Throwable#<init>",
    "kotlin.test#assert",
    "kotlin.test.Asserter",
    "kotlin.test.DefaultAsserter",
  ).any(line::contains)
}
