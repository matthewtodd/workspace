package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;
import com.intellij.openapi.project.Project;

public class BazelExecutionSettings extends ExternalSystemExecutionSettings {

  public BazelExecutionSettings(Project project, String projectPath) {
    // TODO what properties will we want to figure out and add here?
    // Gradle looks like it finds which Gradle to use (bundled, path, wrapper...) and
    // stores a few vm and offline flags.
    // Then it plucks the modules out of ProjectDataManager.getExternalProjectData and adds them to
    // a GradleBuildParticipant added to the execution workspace, a Gradle-specific concept.
  }
}
