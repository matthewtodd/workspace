package org.matthewtodd.intellij.bazel.projectImport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectOpenProcessor;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.matthewtodd.intellij.bazel.BazelManager.BAZEL_PROJECT_NAME;

public class BazelProjectOpenProcessor extends ProjectOpenProcessor {
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
    ProjectManagerEx manager = ProjectManagerEx.getInstanceEx();
    Project project = manager.createProject(System.getProperty(BAZEL_PROJECT_NAME), virtualFile.getPath());

    if (project == null) {
      return null;
    }

    // I don't know if I need this!
    //ExternalProjectsManagerImpl.setupCreatedProject(project);

    //BazelProjectSettings settings = new BazelProjectSettings();
    //settings.setExternalProjectPath(virtualFile.getPath());
    //settings.setUseAutoImport(true);
    //
    //ExternalSystemUtil.linkExternalProject(
    //    BazelManager.SYSTEM_ID,
    //    settings,
    //    project,
    //    null,
    //    false,
    //    ProgressExecutionMode.MODAL_SYNC
    //);

    project.save();
    manager.openProject(project);

    return project;
  }
}
