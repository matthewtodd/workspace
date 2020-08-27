package org.matthewtodd.wake.test

import kotlinx.serialization.Serializable

@Serializable
data class TestResult(
    val className: String,
    val name: String,
    val time: Long,
    val errors: List<TestError>,
    val failures: List<TestFailure>,
    val skipped: List<TestSkip>,
    val systemOut: String,
    val systemErr: String,
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
    val location: String,
)
