package org.matthewtodd.wake.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TestResultTest {
  @Test fun roundTripping() {
    val obj = TestResult(
      class_name = "org.example.Foo",
      name = "bar",
      time = 0.042,
      system_out = "",
      system_err = "",
    )

    assertEquals(obj, Json.decodeFromString(Json.encodeToString(obj)))
  }
}
