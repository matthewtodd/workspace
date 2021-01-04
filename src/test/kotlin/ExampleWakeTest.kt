import org.matthewtodd.wake.test.Test

class ExampleWakeTest {
  @Test fun successful() {
    assertTrue(true)
  }

  @Test fun failed() {
    assertTrue(false, "This is supposed to fail")
  }

  @Test fun error() {
    throw Exception("Boom!")
  }

  // @Test
  // @Ignore
  // fun skipped() = Unit

  fun assertTrue(actual: Boolean, message: String? = null) {
    if (!actual) {
      throw AssertionError(message)
    }
  }
}
