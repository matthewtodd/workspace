package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;

public class BazelExecutionSettings extends ExternalSystemExecutionSettings {
  private final String projectName;
  private final String projectLabel;
  private final String projectPath;

  public BazelExecutionSettings(String projectName, String projectPath, String projectLabel) {
    this.projectName = projectName;
    this.projectLabel = projectLabel;
    this.projectPath = projectPath;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProjectPath() {
    return projectPath;
  }
}
