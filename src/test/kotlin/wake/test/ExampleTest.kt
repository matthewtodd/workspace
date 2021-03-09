package org.matthewtodd.wake.test

class ExampleTest {
  @Test fun successful() {
    assertThat(true).isTrue()
  }

  // @Test fun failed() {
  //   assertThat(true).isFalse()
  // }

  // @Test fun error() {
  //   throw Exception("Boom!")
  // }

  // @Test
  // @Ignore
  // fun skipped() = Unit

  // @Test
  // @Ignore("Reasons.")
  // fun skippedWithMessage() = Unit
}
