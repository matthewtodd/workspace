package org.matthewtodd.wake.test

import org.junit.jupiter.engine.JupiterTestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL
import org.junit.platform.engine.TestExecutionResult.Status.ABORTED
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherFactory
import java.util.Arrays
import java.util.LinkedHashMap
import java.lang.System.currentTimeMillis
import java.util.Arrays.stream
import java.util.stream.Collectors.toList
import org.junit.platform.launcher.core.LauncherConfig.builder
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request

fun main(args:Array<String>) {
  val launcher = LauncherFactory.create(builder().
      enableTestEngineAutoRegistration(false).
      enableTestExecutionListenerAutoRegistration(false).
      addTestEngines(JupiterTestEngine()).
      addTestExecutionListeners(WakeListener()).
      build())

  launcher.execute(request().
      selectors(args.map(DiscoverySelectors::selectClass)).
      build())
}

class WakeListener: TestExecutionListener {
  val startTimes = LinkedHashMap<TestIdentifier, Long>()
  var testPlan: TestPlan? = null

  override fun testPlanExecutionStarted(testPlan:TestPlan) {
    this.testPlan = testPlan
  }

  override fun executionSkipped(testIdentifier:TestIdentifier, reason:String) {
    if (testIdentifier.isContainer()) {
      for (child in testPlan!!.getChildren(testIdentifier)) {
        executionSkipped(child, reason)
      }
    } else {
      // TODO handle other kinds of sources as needed.
      val source = testIdentifier.getSource().get() as MethodSource

      println(JsonObject().
          add("class_name", source.getClassName()).
          add("name", source.getMethodName()).
          add("time", 0).
          addArray("errors").
          addArray("failures").
          addArray("skipped", JsonObject().
              add("message", reason).
              add("location", "")). // TODO can we get this anywhere?
          add("system_out", "").
          add("system_err", ""))
    }
  }

  override fun executionStarted(testIdentifier:TestIdentifier) {
    if (testIdentifier.isTest()) {
      startTimes.put(testIdentifier, currentTimeMillis())
    }
  }

  override fun executionFinished(testIdentifier:TestIdentifier, testExecutionResult:TestExecutionResult) {
    if (testIdentifier.isTest()) {
      // TODO handle other kinds of sources
      val source = testIdentifier.getSource().get() as MethodSource

      val json = JsonObject().
          add("class_name", source.getClassName()).
          add("name", source.getMethodName()).
          add("time", currentTimeMillis() - startTimes.get(testIdentifier)!!)

          when (testExecutionResult.getStatus()!!) {
            SUCCESSFUL -> json.addArray("errors").addArray("failures")
            ABORTED -> throw UnsupportedOperationException("ABORTED comes from JUnit's assume, which kotlin.test doesn't have.")
            FAILED -> {
              val e = testExecutionResult.getThrowable().get()
              if (e is AssertionError) {
                json.
                  addArray("errors").
                  addArray("failures", JsonObject().
                      add("message", e.message!!).
                      add("location", filter(e.stackTrace)[0]))
              } else {
                json.
                  addArray("errors", JsonObject().
                      add("type", e.javaClass.getName()).
                      add("message", e.message!!).
                      add("backtrace", filter(e.stackTrace))).
                  addArray("failures")
              }
            }
          }

      json.addArray("skipped").
          add("system_out", ""). // TODO StreamInterceptingTestExecutionListener
          add("system_err", "") // TODO StreamInterceptingTestExecutionListener

      println(json)
    }
  }

  private fun filter(stackTrace: Array<StackTraceElement>): Array<StackTraceElement> {
    return stackTrace.
      filterNot { it.getClassName().startsWith("jdk.internal") }.
      filterNot { it.getClassName().startsWith("kotlin.test") }.
      takeWhile { !it.getClassName().startsWith("org.junit") }.
      toTypedArray()
  }

  private class JsonObject internal constructor() {
    private val buffer:StringBuilder

    init {
      this.buffer = StringBuilder()
    }

    internal fun add(key:String, value:String):JsonObject {
      return literal(key, quote(escape(value)))
    }

    internal fun add(key:String, value:Number):JsonObject {
      return literal(key, value)
    }

    internal fun add(key:String, value:StackTraceElement):JsonObject {
      return add(key, value.toString())
    }

    internal fun addArray(key:String, vararg values:JsonObject):JsonObject {
      return literal(key, Arrays.toString(values))
    }

    internal fun add(key:String, values:Array<StackTraceElement>):JsonObject {
      return literal(key, Arrays.toString(values.map { quote(escape(it.toString())) }.toTypedArray()))
    }

    public override fun toString():String {
      return "{" + buffer.toString() + "}"
    }

    private fun quote(value:String):String {
      return "\"" + value + "\""
    }

    private fun escape(raw:String):String {
      return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }

    private fun literal(key:String, value:Any):JsonObject {
      buffer.append(String.format("\"%s\":%s,", key, value))
      return this
    }
  }
}
