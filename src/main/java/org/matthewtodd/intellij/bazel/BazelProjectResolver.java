package org.matthewtodd.intellij.bazel;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import java.io.IOException;
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

    // bazel build --aspect= --build_event_proto_thing= settings.getProjectLabel()
    // --> Do I need the aspect? Or is the build event stream enough?
    GeneralCommandLine commandLine = new GeneralCommandLine()
        .withExePath("/usr/local/bin/bazel")
        .withParameters("info")
        .withWorkDirectory(settings.getProjectPath());

    Process process;

    try {
      process = commandLine.createProcess();
    } catch (ExecutionException e) {
      throw new ExternalSystemException(e);
    }

    try {
      process.getOutputStream().close();
    } catch (IOException e) {
      throw new ExternalSystemException(e);
    }

    // TODO spin over process stdout ("input stream") and stderr ("error stream")
    //      and forward to listener.onTaskOutput()
    listener.onTaskOutput(id, "Output!", true);
    listener.onTaskOutput(id, "Error!", false);

    //try {
    //  process.waitFor();
    //} catch (InterruptedException e) {
    //  throw new ExternalSystemException(e);
    //}

    return new DataNode<>(
        ProjectKeys.PROJECT,
        new ProjectData(
            BazelManager.SYSTEM_ID,
            settings.getProjectName(),
            projectPath,
            settings.getProjectPath()
        ),
        null
    );
  }

  @Override public boolean cancelTask(
      @NotNull ExternalSystemTaskId taskId,
      @NotNull ExternalSystemTaskNotificationListener listener) {
    return false;
  }

}
