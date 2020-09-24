package org.matthewtodd.wake.test

import kotlin.js.js
import kotlin.test.FrameworkAdapter

class WakeTest : FrameworkAdapter {
  private val runner = TestRunner()
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

actual fun errorType(e: Throwable): String {
  return stack(e).lineSequence().take(1).joinToString().splitToSequence(":").take(1).joinToString()
}

actual fun errorBacktrace(e: Throwable): List<String> {
  return stack(e).lineSequence().drop(1).dropWhile(::shouldFilter).map(String::trim).toList() // TODO further massage / filter backtrace
}

fun shouldFilter(@Suppress("UNUSED_PARAMETER") line: String): Boolean {
  return listOf(
    "Object.captureStack",
    "/kotlin.js",
    "/kotlin-test.js",
  ).any(line::contains)
}

fun stack(@Suppress("UNUSED_PARAMETER") e: Throwable): String {
  return js("e.stack")
}
