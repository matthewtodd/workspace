package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

expect fun errorType(e: Throwable): String
expect fun errorBacktrace(e: Throwable): List<String>

class TestRunner() {
  @OptIn(ExperimentalTime::class)
  fun test(suiteName: String, name: String, ignored: Boolean, testFn: () -> Any?) {
    val result = TestResultBuilder(suiteName, name).run {
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

    println(Json.encodeToString(result))
  }
}

internal class TestResultBuilder(private val suiteName: String, private val name: String) {
  private val skipped: MutableList<TestSkip> = mutableListOf()
  private val failures: MutableList<TestFailure> = mutableListOf()
  private val errors: MutableList<TestError> = mutableListOf()

  @OptIn(ExperimentalTime::class)
  fun run(go: TestResultBuilder.() -> Unit): TestResult {
    val time = measureTime { this.go() }

    return TestResult(
      class_name = suiteName,
      name = name,
      time = time.inSeconds,
      skipped = skipped,
      failures = failures,
      errors = errors,
      system_out = "",
      system_err = "",
    )
  }

  fun skip() {
    skipped.add(
      TestSkip("@Ignored.")
    )
  }

  fun failure(e: AssertionError) {
    failures.add(
      TestFailure(
        message = e.message!!,
        location = errorBacktrace(e).first(),
      )
    )
  }

  fun error(e: Throwable) {
    errors.add(
      TestError(
        type = errorType(e),
        message = e.message!!,
        backtrace = errorBacktrace(e),
      )
    )
  }
}
