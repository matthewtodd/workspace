import kotlin.test.FrameworkAdapter

class WakeFrameworkAdapter : FrameworkAdapter {
  override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) : Unit {
    suiteFn()
  }

  override fun test(name: String, ignored: Boolean, testFn: () -> Any?) : Unit {
    println(name)
  }
}
