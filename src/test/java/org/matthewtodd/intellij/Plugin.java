package org.matthewtodd.intellij;

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import java.io.File;

import static com.intellij.ide.plugins.PluginManagerCore.PLUGIN_XML;
import static com.intellij.ide.plugins.PluginManagerCore.loadDescriptor;

public class Plugin {
  private final String path;
  private final String descriptor;

  private Plugin(Builder builder) {
    path = builder.path;
    descriptor = builder.descriptor;
  }

  IdeaPluginDescriptorImpl descriptor() {
    return loadDescriptor(new File(System.getProperty("user.dir"), path), descriptor);
  }

  public static Builder plugin() {
    return new Builder();
  }

  public static class Builder {
    String path;
    String descriptor = PLUGIN_XML;

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder builtin(String path) {
      return path("external/intellij/IntelliJ.app/Contents/lib/" + path);
    }

    public Builder descriptor(String descriptor) {
      this.descriptor = descriptor;
      return this;
    }

    Plugin build() {
      return new Plugin(this);
    }
  }
}
