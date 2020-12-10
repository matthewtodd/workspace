package org.matthewtodd.wake.test.runtime

fun test(suiteName: String, name: String, @Suppress("UNUSED_PARAMETER") test: () -> Unit) {
  println("Test: $suiteName.$name")
}
