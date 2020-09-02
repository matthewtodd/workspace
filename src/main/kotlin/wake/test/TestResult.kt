package org.matthewtodd.wake.test

import kotlinx.serialization.Serializable

@Serializable
data class TestResult(
  val class_name: String,
  val name: String,
  val time: Double,
  val errors: List<TestError> = emptyList(),
  val failures: List<TestFailure> = emptyList(),
  val skipped: List<TestSkip> = emptyList(),
  val system_out: String = "",
  val system_err: String = "",
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
