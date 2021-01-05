import org.matthewtodd.wake.test.Ignore
import org.matthewtodd.wake.test.Test
import org.matthewtodd.wake.test.assertThat

class ExampleWakeTest {
  @Test fun successful() {
    assertThat(true).isTrue()
  }

  @Test fun failed() {
    assertThat(true).isFalse()
  }

  @Test fun error() {
    throw Exception("Boom!")
  }

  @Test
  @Ignore
  fun skipped() = Unit

  @Test
  @Ignore("Reasons.")
  fun skippedWithMessage() = Unit
}
