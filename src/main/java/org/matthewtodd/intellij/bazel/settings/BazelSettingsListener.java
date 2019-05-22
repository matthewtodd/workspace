package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.util.messages.Topic;
import java.util.Collection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class BazelSettingsListener
    implements ExternalSystemSettingsListener<BazelProjectSettings> {

  static final Topic<BazelSettingsListener> TOPIC =
      Topic.create("Bazel settings", BazelSettingsListener.class);

  @Override public void onProjectRenamed(@NotNull String oldName, @NotNull String newName) {

  }

  @Override public void onProjectsLinked(@NotNull Collection<BazelProjectSettings> settings) {

  }

  @Override public void onProjectsUnlinked(@NotNull Set<String> linkedProjectPaths) {

  }

  @Override
  public void onUseAutoImportChange(boolean currentValue, @NotNull String linkedProjectPath) {

  }

  @Override public void onBulkChangeStart() {

  }

  @Override public void onBulkChangeEnd() {

  }
}
