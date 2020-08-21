import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class ExampleTest {
  @BeforeTest fun setup() {
  }

  @AfterTest fun teardown() {
  }

  @Test fun successful() {
    assertTrue(true)
  }

  @Test fun failed() {
    assertTrue(false)
  }

  @Test fun error() {
    throw Exception("Boom!")
  }

  @Test @Ignore() fun skipped() {
  }
}
