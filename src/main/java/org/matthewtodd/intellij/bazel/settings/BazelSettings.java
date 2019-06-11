package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BazelSettings extends AbstractExternalSystemSettings<BazelSettings, BazelProjectSettings, BazelSettingsListener> {
  private BazelSettings(Project project) {
    super(BazelSettingsListener.TOPIC, project);
  }

  public static BazelSettings getInstance(Project project) {
    return ServiceManager.getService(project, BazelSettings.class);
  }

  @Override public void subscribe(
      @NotNull ExternalSystemSettingsListener<BazelProjectSettings> listener
  ) {

  }

  @Override protected void copyExtraSettingsFrom(@NotNull BazelSettings bazelSettings) {

  }

  @Override protected void checkSettings(
      @NotNull BazelProjectSettings old,
      @NotNull BazelProjectSettings current
  ) {

  }
}
