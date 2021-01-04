package org.matthewtodd.wake.test.runtime

import kotlin.js.js

actual fun errorType(e: Throwable): String {
  return stack(e).lineSequence().take(1).joinToString().splitToSequence(":").take(1).joinToString()
}

actual fun errorBacktrace(e: Throwable): List<String> {
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
