package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

interface BacktraceInterpreter {
  fun errorType(e: Throwable): String
  fun errorBacktrace(e: Throwable): List<String>
}

class TestRunner(private val backtraceInterpreter: BacktraceInterpreter) {
  @OptIn(ExperimentalTime::class)
  fun test(suiteName: String, name: String, ignored: Boolean, testFn: () -> Any?) {
    val result = TestResultBuilder(suiteName, name, backtraceInterpreter).run {
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

internal class TestResultBuilder(private val suiteName: String, private val name: String, private val backtraceInterpreter: BacktraceInterpreter) {
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
        location = backtraceInterpreter.errorBacktrace(e).first(),
      )
    )
  }

  fun error(e: Throwable) {
    errors.add(
      TestError(
        type = backtraceInterpreter.errorType(e),
        message = e.message!!,
        backtrace = backtraceInterpreter.errorBacktrace(e),
      )
    )
  }
}
