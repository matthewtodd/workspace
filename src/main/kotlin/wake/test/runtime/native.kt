package org.matthewtodd.wake.test.runtime

actual fun errorType(@Suppress("UNUSED_PARAMETER") e: Throwable): String {
  return ""
}

actual fun errorBacktrace(@Suppress("UNUSED_PARAMETER") e: Throwable): List<String> {
  return emptyList()
}
