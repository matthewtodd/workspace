package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings;
import com.intellij.openapi.project.Project;
import org.matthewtodd.intellij.bazel.BazelManager;

public class BazelLocalSettings extends AbstractExternalSystemLocalSettings<AbstractExternalSystemLocalSettings.State> {
  private BazelLocalSettings(Project project) {
    super(BazelManager.SYSTEM_ID, project, new AbstractExternalSystemLocalSettings.State());
  }

  public static BazelLocalSettings getInstance(Project project) {
    return ServiceManager.getService(project, BazelLocalSettings.class);
  }
}
