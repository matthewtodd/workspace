package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.native.internal.test.TestCase
import kotlin.native.internal.test.TestSuite
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class TestRunner(val suites: List<TestSuite>) {
  fun run() {
    suites.forEach { it.run(this) }
  }

  @OptIn(ExperimentalTime::class)
  fun test(testCase: TestCase, run: ResultRecorder.() -> Unit) {
    val recorder = ResultRecorder()

    val time = measureTime {
      recorder.run()
    }

    println(
      Json.encodeToString(
        TestResult(
          class_name = testCase.suite.name,
          name = testCase.name,
          time = time.inSeconds,
          skipped = recorder.skipped,
          failures = recorder.failures,
          errors = recorder.errors,
          system_out = "",
          system_err = "",
        )
      )
    )
  }
}
private fun TestSuite.run(runner: TestRunner) {
  doBeforeClass()

  testCases.values.forEach {
    runner.test(it) {
      if (it.ignored) {
        skip()
      } else {
        try {
          it.run()
        } catch (e: AssertionError) {
          failure(e)
        } catch (e: Throwable) {
          error(e)
        }
      }
    }
  }

  doAfterClass()
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
        location = "",
      )
    )
  }

  fun error(e: Throwable) {
    errors.add(
      TestError(
        type = "",
        message = e.message!!,
        backtrace = emptyList(),
      )
    )
  }
}
