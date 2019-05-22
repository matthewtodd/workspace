package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings;
import org.jetbrains.annotations.NotNull;

public class BazelProjectSettings extends ExternalProjectSettings {
  @Override public @NotNull ExternalProjectSettings clone() {
    return this;
  }
}
