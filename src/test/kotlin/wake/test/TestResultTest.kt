package org.matthewtodd.wake.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.* // TODO understand why we need this * import!
import kotlinx.serialization.json.Json

class TestResultTest {
    @Test fun roundTripping() {
        val obj = TestResult(
            class_name = "org.example.Foo",
            name = "bar",
            time = 42,
        )

        assertEquals(obj, Json.decodeFromString(Json.encodeToString(obj)))
    }
}
