package org.matthewtodd.intellij.bazel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.matthewtodd.intellij.Idea;
import org.matthewtodd.intellij.ProjectTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthewtodd.intellij.Idea.idea;
import static org.matthewtodd.intellij.Plugin.plugin;
import static org.matthewtodd.intellij.bazel.BazelManager.BAZEL_PROJECT_NAME;

public class SmokeTest {
  @Rule public Idea idea = idea()
      .with(plugin().builtin("resources.jar").descriptor("IdeaPlugin.xml"))
      .with(plugin().path("src/main/java/org/matthewtodd/intellij/bazel/libbazel.jar"))
      .build();

  @Rule public TemporaryFolder projectFolder = new TemporaryFolder();
  @Rule public SystemProperties properties = new SystemProperties();

  @Test public void hookup() {
    System.setProperty(BAZEL_PROJECT_NAME, "foo");

    try(ProjectTester project = idea.open(projectFolder.getRoot())) {
      assertThat(project.opened()).isTrue();
      assertThat(project.name()).isEqualTo("foo");
    }
  }

  private static class SystemProperties implements TestRule {
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @Override public Statement apply(Statement statement, Description description) {
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          stashProperties();
          try {
            statement.evaluate();
          } finally {
            restoreProperties();
          }
        }
      };
    }

    private void stashProperties() throws IOException {
      buffer.reset();
      Properties props = System.getProperties();
      props.store(buffer, "no comment");
    }

    private void restoreProperties() throws IOException {
      Properties props = new Properties();
      props.load(new ByteArrayInputStream(buffer.toByteArray()));
      System.setProperties(props);
    }
  }
}
