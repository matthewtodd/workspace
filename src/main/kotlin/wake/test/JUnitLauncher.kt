package org.matthewtodd.wake.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.engine.JupiterTestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.ABORTED
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherConfig.builder
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.junit.platform.launcher.core.LauncherFactory
import java.util.LinkedHashMap
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

fun main(args: Array<String>) {
  val launcher = LauncherFactory.create(
    builder()
      .enableTestEngineAutoRegistration(false)
      .enableTestExecutionListenerAutoRegistration(false)
      .addTestEngines(JupiterTestEngine())
      .addTestExecutionListeners(WakeListener())
      .build()
  )

  launcher.execute(
    request()
      .selectors(DiscoverySelectors.selectClasspathRoots(args.map { java.nio.file.Path.of(it) }.toSet()))
      .build()
  )
}

@OptIn(ExperimentalTime::class)
class WakeListener : TestExecutionListener {
  val startTimes = LinkedHashMap<TestIdentifier, TimeMark>()
  var testPlan: TestPlan? = null

  override fun testPlanExecutionStarted(testPlan: TestPlan) {
    this.testPlan = testPlan
  }

  override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
    if (testIdentifier.isContainer()) {
      for (child in testPlan!!.getChildren(testIdentifier)) {
        executionSkipped(child, reason)
      }
    } else {
      // TODO handle other kinds of sources as needed.
      val source = testIdentifier.getSource().get() as MethodSource

      println(
        Json.encodeToString(
          TestResult(
            class_name = source.getClassName(),
            name = source.getMethodName(),
            time = 0.0,
            skipped = listOf(TestSkip(reason)),
            system_out = "",
            system_err = "",
          )
        )
      )
    }
  }

  override fun executionStarted(testIdentifier: TestIdentifier) {
    if (testIdentifier.isTest()) {
      startTimes.put(testIdentifier, TimeSource.Monotonic.markNow())
    }
  }

  override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
    if (testIdentifier.isTest()) {
      // TODO handle other kinds of sources
      val source = testIdentifier.getSource().get() as MethodSource

      val base = TestResult(
        class_name = source.getClassName(),
        name = source.getMethodName(),
        time = startTimes.get(testIdentifier)!!.elapsedNow().inSeconds,
        system_out = "", // TODO StreamInterceptingTestExecutionListener
        system_err = "", // TODO StreamInterceptingTestExecutionListener
      )

      val result = when (testExecutionResult.getStatus()!!) {
        SUCCESSFUL -> base
        ABORTED -> throw UnsupportedOperationException("ABORTED comes from JUnit's assume, which kotlin.test doesn't have.")
        FAILED -> {
          val e = testExecutionResult.getThrowable().get()
          if (e is AssertionError) {
            base.copy(
              failures = listOf(
                TestFailure(
                  message = e.message!!,
                  location = filter(e.stackTrace)[0].toString()
                )
              )
            )
          } else {
            base.copy(
              errors = listOf(
                TestError(
                  type = e.javaClass.getName(),
                  message = e.message!!,
                  backtrace = filter(e.stackTrace).map { it.toString() },
                )
              )
            )
          }
        }
      }

      println(
        Json.encodeToString(result)
      )
    }
  }

  private fun filter(stackTrace: Array<StackTraceElement>): List<StackTraceElement> {
    return stackTrace
      .filterNot { it.getClassName().startsWith("jdk.internal") }
      .filterNot { it.getClassName().startsWith("kotlin.test") }
      .takeWhile { !it.getClassName().startsWith("org.junit") }
  }
}
