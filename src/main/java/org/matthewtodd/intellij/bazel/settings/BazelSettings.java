package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BazelSettings extends AbstractExternalSystemSettings<BazelSettings, BazelProjectSettings, BazelSettingsListener> {
  public BazelSettings(Project project) {
    super(BazelSettingsListener.TOPIC, project);
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
