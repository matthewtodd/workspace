package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings.State;
import com.intellij.openapi.project.Project;
import org.matthewtodd.intellij.bazel.BazelManager;

public class BazelLocalSettings extends AbstractExternalSystemLocalSettings<State> {
  public BazelLocalSettings(Project project) {
    super(BazelManager.SYSTEM_ID, project, new State());
  }
}
