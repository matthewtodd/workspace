package org.matthewtodd.wake.test

import kotlin.test.FrameworkAdapter

// https://mochajs.org/#third-party-reporters
// https://github.com/mochajs/mocha-examples/blob/master/packages/third-party-reporter/lib/my-reporter.js
class WakeTest : FrameworkAdapter {
  // I think we're going to make a data class that gets serialized to JSON?
  override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
    println(name)
    suiteFn()
  }

  override fun test(name: String, ignored: Boolean, testFn: () -> Any?) {
    // TODO handle ignored
    try {
      testFn()
      println(name)
    } catch (e: AssertionError) {
      println(e.message)
    } catch (t: Throwable) {
      println(t.message)
    }
  }
}
