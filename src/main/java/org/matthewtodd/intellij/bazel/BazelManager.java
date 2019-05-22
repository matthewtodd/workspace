package org.matthewtodd.intellij.bazel;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.matthewtodd.intellij.bazel.settings.BazelExecutionSettings;
import org.matthewtodd.intellij.bazel.settings.BazelLocalSettings;
import org.matthewtodd.intellij.bazel.settings.BazelProjectSettings;
import org.matthewtodd.intellij.bazel.settings.BazelSettings;
import org.matthewtodd.intellij.bazel.settings.BazelSettingsListener;

public class BazelManager implements ExternalSystemManager<
    BazelProjectSettings,
    BazelSettingsListener,
    BazelSettings,
    BazelLocalSettings,
    BazelExecutionSettings> {

  public static final ProjectSystemId SYSTEM_ID =
      new ProjectSystemId("org.matthewtodd.intellij.bazel");

  @Override public @NotNull ProjectSystemId getSystemId() {
    return SYSTEM_ID;
  }

  @Override public @NotNull Function<Project, BazelSettings> getSettingsProvider() {
    return BazelSettings::new;
  }

  @Override public @NotNull Function<Project, BazelLocalSettings> getLocalSettingsProvider() {
    return BazelLocalSettings::new;
  }

  @Override public @NotNull
  Function<Pair<Project, String>, BazelExecutionSettings> getExecutionSettingsProvider() {
    return pair -> new BazelExecutionSettings(pair.first, pair.second);
  }

  @Override public @NotNull
  Class<? extends ExternalSystemProjectResolver<BazelExecutionSettings>> getProjectResolverClass() {
    return BazelProjectResolver.class;
  }

  @Override public @NotNull
  Class<? extends ExternalSystemTaskManager<BazelExecutionSettings>> getTaskManagerClass() {
    return BazelTaskManager.class;
  }

  @Override public @NotNull FileChooserDescriptor getExternalProjectDescriptor() {
    return new FileChooserDescriptor(true, false, false, false, false, false)
        .withFileFilter(file -> file.getName().equals("WORKSPACE"));
  }

  @Override public void enhanceRemoteProcessing(@NotNull SimpleJavaParameters parameters)
      throws ExecutionException {
    // not sure if we need to do anything here...
  }
}
