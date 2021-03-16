package org.matthewtodd.learning

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.matthewtodd.wake.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

// TODO compiler parameters!
// TODO -Xopt-in=kotlin.time.ExperimentalTime
@OptIn(ExperimentalTime::class)
class CoroutinesTest {
  // TODO suspend fun is automatically runBlocking by wake.test
  // Or perhaps we want runBlockingTest,
  // Perhaps with some affordance for working with the DelayController.
  @Test fun successful() = runBlocking {
    @Suppress("UNUSED_VARIABLE")
    val timer = Timer(3.minutes)

    val flow = flow {
      emit(42)
    }

    flow.test {
      expectItem()
      expectComplete()
    }
  }

  class Timer(private val duration: Duration) {
    enum class State {
      READY, RUNNING, PAUSED, DONE
    }
  }
}
