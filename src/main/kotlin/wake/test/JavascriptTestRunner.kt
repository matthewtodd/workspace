package org.matthewtodd.wake.test

import kotlin.js.js
import kotlin.test.FrameworkAdapter

class WakeTest : FrameworkAdapter {
  private val runner = TestRunner(JavascriptBacktraceInterpreter())
  private val suiteNames: MutableList<String> = mutableListOf()

  override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
    if (name.isNotBlank()) {
      suiteNames.add(name)
    }
    suiteFn()
    if (name.isNotBlank()) {
      suiteNames.removeAt(suiteNames.count() - 1)
    }
  }

  override fun test(name: String, ignored: Boolean, testFn: () -> Any?) {
    runner.test(suiteNames.joinToString("."), name, ignored, testFn)
  }
}

class JavascriptBacktraceInterpreter : BacktraceInterpreter {
  override fun errorType(e: Throwable): String {
    return stack(e).lineSequence().take(1).joinToString().splitToSequence(":").take(1).joinToString()
  }

  override fun errorBacktrace(e: Throwable): List<String> {
    return stack(e).lineSequence().drop(1).dropWhile(::shouldFilter).map(String::trim).toList() // TODO further massage / filter backtrace
  }

  private fun shouldFilter(line: String): Boolean {
    return listOf(
      "Object.captureStack",
      "/kotlin.js",
      "/kotlin-test.js",
    ).any(line::contains)
  }

  private fun stack(@Suppress("UNUSED_PARAMETER") e: Throwable): String {
    return js("e.stack")
  }
}
