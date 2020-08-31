package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

  class SuiteListener() {
    val stack: MutableList<String> = mutableListOf()

    fun begin(name: String) {
      stack.add(name)
    }

    fun end() {
      stack.removeAt(stack.count() - 1)
    }

    fun test(name: String): TestListener {
      return TestListener(stack.joinToString("."), name)
    }
  }

  class TestListener(suiteName: String, name: String) {
    val template = TestResult(
      class_name = suiteName,
      name = name,
      time = 0,
    )

    fun success() {
      emit(template)
    }

    fun skip() {
      emit(template.copy(skipped = listOf(TestSkip())))
    }

    fun failure(e: AssertionError) {
      emit(template.copy(failures = listOf(TestFailure(e.message!!, location = "TODO"))))
    }

    fun error(e: Throwable) {
      emit(template.copy(errors = listOf(TestError("TODO", e.message!!, backtrace = listOf("TODO")))))
    }

    private fun emit(r: TestResult) {
      println(Json.encodeToString(r))
    }
  }
}
