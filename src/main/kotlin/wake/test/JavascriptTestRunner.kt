package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.js
import kotlin.test.FrameworkAdapter
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class WakeTest : FrameworkAdapter {
  private val runner = TestRunner()

  override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
    runner.suite(name, suiteFn)
  }

  override fun test(name: String, ignored: Boolean, testFn: () -> Any?) {
    runner.test(name) {
      if (ignored) {
        skip()
      } else {
        try {
          testFn()
        } catch (e: AssertionError) {
          failure(e)
        } catch (e: Throwable) {
          error(e)
        }
      }
    }
  }

  class TestRunner() {
    private val suiteNames: MutableList<String> = mutableListOf()

    fun suite(name: String, suiteFn: () -> Unit) {
      suiteNames.add(name)
      suiteFn()
      suiteNames.removeAt(suiteNames.count() - 1)
    }

    @OptIn(ExperimentalTime::class)
    fun test(name: String, run: ResultRecorder.() -> Unit) {
      val recorder = ResultRecorder()

      val time = measureTime {
        recorder.run()
      }

      println(
        Json.encodeToString(
          TestResult(
            class_name = suiteNames.joinToString("."),
            name = name,
            time = time.inSeconds,
            skipped = recorder.skipped,
            failures = recorder.failures,
            errors = recorder.errors,
          )
        )
      )
    }
  }

  class ResultRecorder() {
    val skipped: MutableList<TestSkip> = mutableListOf()
    val failures: MutableList<TestFailure> = mutableListOf()
    val errors: MutableList<TestError> = mutableListOf()

    fun skip() {
      skipped.add(
        TestSkip("@Ignored.")
      )
    }

    fun failure(e: AssertionError) {
      failures.add(
        TestFailure(
          message = e.message!!,
          location = stack(e).lineSequence().drop(1).take(1).joinToString(), // TODO further massage location
        )
      )
    }

    fun error(e: Throwable) {
      errors.add(
        TestError(
          type = stack(e).lineSequence().take(1).joinToString().splitToSequence(":").take(1).joinToString(),
          message = e.message!!,
          backtrace = stack(e).lineSequence().drop(1).toList(), // TODO further massage / filter backtrace
        )
      )
    }

    fun stack(@Suppress("UNUSED_PARAMETER") e: Throwable): String {
      return js("e.stack")
    }
  }
}
