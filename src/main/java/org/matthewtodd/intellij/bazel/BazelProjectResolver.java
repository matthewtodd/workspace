package org.matthewtodd.intellij.bazel;

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import org.jetbrains.annotations.NotNull;
import org.matthewtodd.intellij.bazel.settings.BazelExecutionSettings;

public class BazelProjectResolver implements ExternalSystemProjectResolver<BazelExecutionSettings> {

  @Override public DataNode<ProjectData> resolveProjectInfo(
      @NotNull ExternalSystemTaskId id,
      @NotNull String projectPath,
      boolean isPreviewMode,
      BazelExecutionSettings settings,
      @NotNull ExternalSystemTaskNotificationListener listener
  ) throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
    // TODO here's where we need to invoke Bazel with our aspect to gather up a tree of DataNodes.
    // TODO we also need some way to read the external name for the workspace.
    return new DataNode<>(
        ProjectKeys.PROJECT,
        new ProjectData(BazelManager.SYSTEM_ID, projectPath, projectPath, projectPath),
        null
    );
  }

  @Override public boolean cancelTask(
      @NotNull ExternalSystemTaskId taskId,
      @NotNull ExternalSystemTaskNotificationListener listener) {
    return false;
  }

}
