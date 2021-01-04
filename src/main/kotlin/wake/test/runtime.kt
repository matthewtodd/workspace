package org.matthewtodd.wake.test.runtime

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun test(suiteName: String, name: String, test: () -> Unit) {
  val errors: MutableList<TestError> = mutableListOf()
  val failures: MutableList<TestFailure> = mutableListOf()

  val time = measureTime {
    try {
      test()
    } catch (e: AssertionError) {
      failures.add(TestFailure(e.message!!, "<unspecified>"))
    } catch (e: Throwable) {
      errors.add(TestError("UNKNOWN", e.message!!, listOf()))
    }
  }

  println(
    Json.encodeToString(
      TestResult(
        class_name = suiteName,
        name = name,
        time = time.inSeconds,
        errors = errors,
        failures = failures,
      )
    )
  )
}

@Serializable
data class TestResult(
  val class_name: String,
  val name: String,
  val time: Double,
  val errors: List<TestError> = emptyList(),
  val failures: List<TestFailure> = emptyList(),
  val skipped: List<TestSkip> = emptyList(),
)

@Serializable
data class TestError(
  val type: String,
  val message: String,
  val backtrace: List<String>
)

@Serializable
data class TestFailure(
  val message: String,
  val location: String,
)

@Serializable
data class TestSkip(
  val message: String,
)
