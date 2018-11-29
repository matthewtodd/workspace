package org.matthewtodd.perquackey.console;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.matthewtodd.console.View;
import org.matthewtodd.workflow.WorkflowScreen;

class ViewFactory {
  private final Function<String, View> registry;
  private final List<Binding> bindings;

  static Builder newBuilder(Function<String, View> registry) {
    return new Builder(registry);
  }

  static class Builder {
    private final Function<String, View> registry;
    private final List<Binding> bindings = new LinkedList<>();

    private Builder(Function<String, View> registry) {
      this.registry = registry;
    }

    Builder bind(String key, String id, Function<WorkflowScreen<?, ?>, Coordinator> coordinatorProvider) {
      bindings.add(new Binding(key, id, coordinatorProvider));
      return this;
    }

    ViewFactory build() {
      return new ViewFactory(registry, bindings);
    }
  }

  private ViewFactory(Function<String, View> registry, List<Binding> bindings) {
    this.registry = registry;
    this.bindings = Collections.unmodifiableList(bindings);
  }

  View get(WorkflowScreen<?, ?> screen) {
    return bindings.stream()
        .map(b -> b.get(screen, registry))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  private static class Binding {
    private final String key;
    private final String id;
    private final Function<WorkflowScreen<?, ?>, Coordinator> coordinatorProvider;

    private Binding(String key, String id,
        Function<WorkflowScreen<?, ?>, Coordinator> coordinatorProvider) {
      this.key = key;
      this.id = id;
      this.coordinatorProvider = coordinatorProvider;
    }

    private View get(WorkflowScreen<?, ?> screen, Function<String, View> registry) {
      if (key.equals(screen.key)) {
        Coordinator c = coordinatorProvider.apply(screen);
        return registry.apply(id).attachmentListeners(c::attach, c::detach);
      }
      return null;
    }
  }
}
