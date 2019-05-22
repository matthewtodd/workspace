package org.matthewtodd.intellij.bazel;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BazelProjectImportBuilder extends ProjectImportBuilder {
  @NotNull @Override public String getName() {
    return null;
  }

  @Override public Icon getIcon() {
    return null;
  }

  @Override public List getList() {
    return null;
  }

  @Override public boolean isMarked(Object o) {
    return false;
  }

  @Override public void setList(List list) throws ConfigurationException {

  }

  @Override public void setOpenProjectSettingsAfter(boolean b) {

  }

  @Nullable @Override
  public List<Module> commit(Project project, ModifiableModuleModel modifiableModuleModel,
      ModulesProvider modulesProvider, ModifiableArtifactModel modifiableArtifactModel) {
    return null;
  }
}
