package org.matthewtodd.wake.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.* // TODO understand why we need this * import!
import kotlinx.serialization.json.Json

class TestResultTest {
    @Test fun roundTripping() {
        val obj = TestResult(
            className = "org.example.Foo",
            name = "bar",
            time = 42,
            errors = emptyList(),
            failures = emptyList(),
            skipped = emptyList(),
            systemOut = "",
            systemErr = "",
        )

        assertEquals(obj, Json.decodeFromString(Json.encodeToString(obj)))
    }
}
