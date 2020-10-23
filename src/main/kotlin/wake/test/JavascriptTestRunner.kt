package org.matthewtodd.wake.test

import kotlin.js.js

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

internal external val global: dynamic

fun main() {
  val runner = TestRunner(JavascriptBacktraceInterpreter())

  val suiteNames: MutableList<String> = mutableListOf()

  global.describe = fun(name: String, fn: () -> Unit) {
    if (name.isNotBlank()) {
      suiteNames.add(name)
    }

    fn()

    if (name.isNotBlank()) {
      suiteNames.removeAt(suiteNames.count() - 1)
    }
  }

  global.xdescribe = fun(_: String, _: () -> Unit) { }

  global.it = fun(name: String, fn: () -> Any?) {
    runner.test(suiteNames.joinToString("."), name, false, fn)
  }

  global.xit = fun(name: String, fn: () -> Any?) {
    runner.test(suiteNames.joinToString("."), name, true, fn)
  }
}
