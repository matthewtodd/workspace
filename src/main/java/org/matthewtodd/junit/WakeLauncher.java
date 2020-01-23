package org.matthewtodd.junit;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.launcher.core.LauncherConfig.builder;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class WakeLauncher {
  public static void main(String... args) {
    Launcher launcher = LauncherFactory.create(builder().
        enableTestEngineAutoRegistration(false).
        enableTestExecutionListenerAutoRegistration(false).
        addTestEngines(new JupiterTestEngine()).
        addTestExecutionListeners(new WakeListener()).
        build());

    launcher.execute(request().
        selectors(stream(args).
            map(DiscoverySelectors::selectClass).
            collect(toList())).
        build());
  }

  private static class WakeListener implements TestExecutionListener {
    private Map<TestIdentifier, Long> startTimes = new LinkedHashMap<>();
    private TestPlan testPlan;

    public void testPlanExecutionStarted(TestPlan testPlan) {
      this.testPlan = testPlan;
    }

    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
      if (testIdentifier.isContainer()) {
        for (TestIdentifier child : testPlan.getChildren(testIdentifier)) {
          executionSkipped(child, reason);
        }
      } else {
        // TODO handle other kinds of sources
        MethodSource source = (MethodSource) testIdentifier.getSource().get();

        System.out.println(new JsonObject().
            add("class_name", source.getClassName()).
            add("name", source.getMethodName()).
            add("time", 0).
            addArray("errors").
            addArray("failures").
            addArray("skipped", new JsonObject().
                add("message", reason).
                add("location", "")). // TODO
            add("system_out", "").
            add("system_err", ""));
      }
    }

    public void executionStarted(TestIdentifier testIdentifier) {
      if (testIdentifier.isTest()) {
        startTimes.put(testIdentifier, currentTimeMillis());
      }
    }

    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
      if (testIdentifier.isTest()) {
        // TODO handle other kinds of sources
        MethodSource source = (MethodSource) testIdentifier.getSource().get();

        JsonObject json = new JsonObject().
            add("class_name", source.getClassName()).
            add("name", source.getMethodName()).
            add("time", currentTimeMillis() - startTimes.get(testIdentifier));

        switch(testExecutionResult.getStatus()) {
          case SUCCESSFUL:
            json.addArray("errors").addArray("failures");
            break;
          case ABORTED:
            throw new UnsupportedOperationException("ABORTED comes from JUnit's assume, which kotlin.test doesn't have.");
          case FAILED:
            Throwable e = testExecutionResult.getThrowable().get();
            if (e instanceof AssertionError) {
              json.
                addArray("errors").
                addArray("failures", new JsonObject().
                    add("message", e.getMessage()).
                    add("location", filter(e.getStackTrace())[0]));
            } else {
              json.
                addArray("errors", new JsonObject().
                    add("type", e.getClass().getName()).
                    add("message", e.getMessage()).
                    add("backtrace", filter(e.getStackTrace()))).
                addArray("failures");
            }
            break;
        }

        json.addArray("skipped").
            add("system_out", ""). // TODO StreamInterceptingTestExecutionListener
            add("system_err", ""); // TODO StreamInterceptingTestExecutionListener

        System.out.println(json);
      }
    }

    private StackTraceElement[] filter(StackTraceElement[] stackTrace) {
      return Arrays.stream(stackTrace).
          filter(e -> !e.getClassName().startsWith("jdk.internal")).
          filter(e -> !e.getClassName().startsWith("kotlin.test")).
          takeWhile(e -> !e.getClassName().startsWith("org.junit")).
          toArray(StackTraceElement[]::new);
    }

    private static class JsonObject {
      private final StringBuilder buffer;

      JsonObject() {
        this.buffer = new StringBuilder();
      }

      JsonObject add(String key, String value) {
        return literal(key, quote(escape(value)));
      }

      JsonObject add(String key, Number value) {
        return literal(key, value);
      }

      JsonObject add(String key, StackTraceElement value) {
        return add(key, value.toString());
      }

      JsonObject addArray(String key, JsonObject... values) {
        return literal(key, Arrays.toString(values));
      }

      JsonObject add(String key, StackTraceElement[] values) {
        return literal(key, Arrays.toString(
            Arrays.stream(values).
                map(v -> quote(escape(v.toString()))).
                toArray(String[]::new)));
      }

      public String toString() {
        return "{" + buffer.toString() + "}";
      }

      private String quote(String value) {
        return "\"" + value + "\"";
      }

      private String escape(String raw) {
        return raw.
            replace("\\", "\\\\").
            replace("\"", "\\\"").
            replace("\n", "\\n");
      }

      private JsonObject literal(String key, Object value) {
        buffer.append(String.format("\"%s\":%s,", key, value));
        return this;
      }
    }
  }
}

