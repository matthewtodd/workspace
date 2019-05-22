package org.matthewtodd.intellij.bazel;

import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.matthewtodd.intellij.bazel.settings.BazelExecutionSettings;

public class BazelTaskManager implements ExternalSystemTaskManager<BazelExecutionSettings> {

  @Override public void executeTasks(
      @NotNull ExternalSystemTaskId id,
      @NotNull List<String> taskNames,
      @NotNull String projectPath,
      BazelExecutionSettings settings,
      String jvmAgentSetup,
      @NotNull ExternalSystemTaskNotificationListener listener
  ) throws ExternalSystemException {

  }

  @Override public boolean cancelTask(
      @NotNull ExternalSystemTaskId id,
      @NotNull ExternalSystemTaskNotificationListener listener
  ) throws ExternalSystemException {
    return false;
  }

}
