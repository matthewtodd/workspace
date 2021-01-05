package org.matthewtodd.wake.test

@Target(AnnotationTarget.FUNCTION)
annotation class Test

@Target(AnnotationTarget.FUNCTION)
annotation class Ignore(val message: String = "Ignored.")

fun assertThat(actual: Boolean) = BooleanSubject(actual)

class BooleanSubject(val actual: Boolean) {
  fun isTrue() {
    if (!actual) {
      throw AssertionError("expected to be true")
    }
  }

  fun isFalse() {
    if (actual) {
      throw AssertionError("expected to be false")
    }
  }
}
