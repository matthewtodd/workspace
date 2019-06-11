package org.matthewtodd.intellij.bazel.settings;

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.util.messages.Topic;
import java.util.Collection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface BazelSettingsListener
    extends ExternalSystemSettingsListener<BazelProjectSettings> {

  Topic<BazelSettingsListener> TOPIC =
      Topic.create("Bazel settings", BazelSettingsListener.class);

  @Override default void onProjectRenamed(@NotNull String oldName, @NotNull String newName) {}

  @Override default void onProjectsLinked(@NotNull Collection<BazelProjectSettings> settings) {}

  @Override default void onProjectsUnlinked(@NotNull Set<String> linkedProjectPaths) {}

  @Override default void onUseAutoImportChange(boolean currentValue, @NotNull String linkedProjectPath) {}

  @Override default void onBulkChangeStart() {}

  @Override default void onBulkChangeEnd() {}
}
