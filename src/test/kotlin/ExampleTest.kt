import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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

  // Uncomment these tests when working on test output formatting.
  // Also make Wake.run unconditionally return 0.

  // @Test fun failed() {
  //   import kotlin.test.assertEquals
  //   assertEquals(true, false, "This is supposed to fail")
  // }

  // @Test fun error() {
  //   throw Exception("Boom!")
  // }

  // @Test
  // @Ignore()
  // fun skipped() {
  // }
}
