package org.matthewtodd.learning

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.matthewtodd.wake.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@OptIn(ExperimentalTime::class)
class CoroutinesTest {

  @Test fun successful() = runBlocking {
    val timer = Timer(3.minutes)

    timer.remaining().test {
      expectItem()
      expectComplete()
    }
  }

  class Timer(private val duration: Duration) {
    fun start() {}

    fun remaining(): Flow<Duration> {
      return flowOf(duration)
    }
  }
}
