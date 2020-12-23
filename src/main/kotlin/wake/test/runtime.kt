package org.matthewtodd.wake.test.runtime

fun test(@Suppress("UNUSED_PARAMETER") suiteName: String, @Suppress("UNUSED_PARAMETER") name: String, @Suppress("UNUSED_PARAMETER") test: () -> Unit) {
  test()
}
