package org.matthewtodd.wake.test

import kotlin.test.FrameworkAdapter

class WakeTest : FrameworkAdapter {
  private val suite = SuiteListener()

  override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
    suite.begin(name)
    suiteFn()
    suite.end()
  }

  override fun test(name: String, ignored: Boolean, testFn: () -> Any?) {
    val test = suite.test(name)

    if (ignored) {
      test.skip()
      return
    }

    try {
      testFn()
      test.success()
    } catch (e: AssertionError) {
      test.failure(e)
    } catch (e: Throwable) {
      test.error(e)
    }
  }

  class SuiteListener {
    val stack: MutableList<String> = mutableListOf()

    fun begin(name: String) {
      stack.add(name)
    }

    fun test(name: String): TestListener {
      return TestListener(stack.joinToString("."), name)
    }

    fun end() {
      stack.removeAt(stack.count() - 1)
    }
  }

  class TestListener(suiteName: String, name: String) {
    val template = TestResult(
      class_name = suiteName,
      name = name,
      time = 0,
    )

    fun success() {
    }

    fun skip() {
    }

    fun failure(e: AssertionError) {
      println(e.message)
    }

    fun error(t: Throwable) {
      println(t.message)
    }
  }
}
