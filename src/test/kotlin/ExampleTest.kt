import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class ExampleTest {
  @Test fun successful() {
    assertTrue(true)
  }

  @Test fun failed() {
    assertTrue(false)
  }

  @Test fun error() {
    throw Exception("Boom!")
  }

  @Test @Ignore("Reason.") fun skipped() {
  }
}
