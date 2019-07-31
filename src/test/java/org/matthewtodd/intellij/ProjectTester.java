package org.matthewtodd.intellij;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;

import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;

public class ProjectTester implements AutoCloseable {
  private final Project project;

  ProjectTester(Project project) {
    this.project = project;
  }

  public String name() {
    return project.getName();
  }

  public boolean opened() {
    return ProjectManagerEx.getInstanceEx().isProjectOpened(project);
  }

  @Override public void close() {
    runInEdtAndWait(() -> ProjectUtil.closeAndDispose(project));
  }
}
