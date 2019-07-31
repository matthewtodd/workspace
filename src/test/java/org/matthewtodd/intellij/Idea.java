package org.matthewtodd.intellij;

import com.intellij.ide.ClassUtilCore;
import com.intellij.ide.RecentProjectsManagerBase;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.idea.IdeaTestApplication;
import com.intellij.openapi.extensions.AreaInstance;
import com.intellij.openapi.extensions.AreaListener;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.project.Project;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static com.intellij.ide.impl.ProjectUtil.openOrImport;
import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndGet;
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;
import static org.matthewtodd.intellij.Plugin.plugin;

public class Idea implements TestRule {
  private final List<Plugin> plugins;
  private Project project;

  private Idea(Builder builder) {
    plugins = builder.plugins;
  }

  public static Builder idea() {
    return new Builder();
  }

  @Override public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        loadPlugins();
        try(AutoCloseable instance = launchInstance()) {
          statement.evaluate();
        }
      }
    };
  }

  public ProjectTester open(File projectPath) {
    project = runInEdtAndGet(() -> openOrImport(projectPath.getAbsolutePath(), null, true));
    return new ProjectTester(project);
  }

  private void loadPlugins() {
    System.setProperty("idea.plugins.load", "false");
    assert ClassUtilCore.isLoadingOfExternalPluginsDisabled();

    PluginManagerCore.configureExtensions();

    PluginManagerCore.setPlugins(this.plugins.stream()
        .map(Plugin::descriptor)
        .toArray(IdeaPluginDescriptor[]::new));

    for (IdeaPluginDescriptor plugin : PluginManagerCore.getPlugins()) {
      registerExtensions(((IdeaPluginDescriptorImpl) plugin));
    }
  }

  private void registerExtensions(IdeaPluginDescriptorImpl plugin) {
    ExtensionsArea area = Extensions.getRootArea();
    plugin.registerExtensionPoints(area);
    for (ExtensionPoint extensionPoint : area.getExtensionPoints()) {
      plugin.registerExtensions(area, extensionPoint);
    }

    Extensions.AREA_LISTENER_EXTENSION_POINT.getPoint(null).registerExtension(new AreaListener() {
      @Override public void areaCreated(@NotNull String areaClass, @NotNull
          AreaInstance areaInstance) {
        ExtensionsArea area1 = Extensions.getArea(areaInstance);
        plugin.registerExtensionPoints(area1);
        for (ExtensionPoint extensionPoint : area1.getExtensionPoints()) {
          plugin.registerExtensions(area1, extensionPoint);
        }
      }
    });
  }

  private AutoCloseable launchInstance() {
    IdeaTestApplication application = IdeaTestApplication.getInstance();
    return () -> runInEdtAndWait(() -> getApplication().runWriteAction(application::dispose));
  }

  public static class Builder {
    final List<Plugin> plugins = new ArrayList<>();

    public Builder with(Plugin.Builder plugin) {
      plugins.add(plugin.build());
      return this;
    }

    public Idea build() {
      return new Idea(this);
    }
  }
}
