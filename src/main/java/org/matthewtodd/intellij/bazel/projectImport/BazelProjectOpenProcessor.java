package org.matthewtodd.intellij.bazel.projectImport;

import com.intellij.ide.RecentProjectsManagerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectOpenProcessor;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.matthewtodd.intellij.bazel.BazelManager;
import org.matthewtodd.intellij.bazel.settings.BazelProjectSettings;

public class BazelProjectOpenProcessor extends ProjectOpenProcessor {
  private static final Logger logger = Logger.getInstance(BazelProjectOpenProcessor.class);

  @Override public String getName() {
    return "Bazel";
  }

  @Nullable @Override public Icon getIcon() {
    return null;
  }

  @Override public boolean canOpenProject(VirtualFile file) {
    return true;
  }

  @Override public boolean isStrongProjectInfoHolder() {
    return true;
  }

  @Nullable @Override
  public Project doOpenProject(@NotNull VirtualFile virtualFile, @Nullable Project projectToClose, boolean forceOpenInNewFrame) {
    final ProjectManagerEx manager = ProjectManagerEx.getInstanceEx();

    final Project project = manager.createProject(
        System.getProperty("bazel.project.name"),
        virtualFile.getPath()
    );

    if (project == null) {
      return null;
    }

    // I don't know if I need this!
    //ExternalProjectsManagerImpl.setupCreatedProject(project);

    BazelProjectSettings settings = new BazelProjectSettings();
    settings.setExternalProjectPath(virtualFile.getPath());
    settings.setUseAutoImport(true);

    ExternalSystemUtil.linkExternalProject(
        BazelManager.SYSTEM_ID,
        settings,
        project,
        null,
        false,
        ProgressExecutionMode.MODAL_SYNC
    );

    project.save();
    manager.openProject(project);

    // Clear recent projects list!
    RecentProjectsManagerBase.getInstanceEx()
        .loadState(new RecentProjectsManagerBase.State());

    return project;
  }
}
