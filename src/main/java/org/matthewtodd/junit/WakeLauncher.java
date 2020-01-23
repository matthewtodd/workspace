package org.matthewtodd.junit;

import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.launcher.core.LauncherConfig.builder;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class WakeLauncher {
  // TODO consider StreamInterceptingTestExecutionListener
  // doesn't look thread-safe
  // so maybe we let wake do the parallelism, making individual test classes small and counting on dependency tracking to avoid unnecessary execution?
  public static void main(String... args) {
    Launcher launcher = LauncherFactory.create(builder().
        enableTestExecutionListenerAutoRegistration(false).
        enableTestEngineAutoRegistration(false).
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
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
    }

    public void executionStarted(TestIdentifier testIdentifier) {
      // System.out.println(testIdentifier);
    }

    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
      System.out.println("--");
      System.out.println(testIdentifier);
      System.out.println(testExecutionResult);
    }
  }
}

