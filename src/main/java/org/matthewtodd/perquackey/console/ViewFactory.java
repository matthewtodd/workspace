package org.matthewtodd.perquackey.console;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.matthewtodd.console.View;
import org.matthewtodd.workflow.WorkflowScreen;

class ViewFactory implements Function<WorkflowScreen<?, ?>, View> {
  private final Function<String, View> views;
  private final Function<String, String> bindings;
  private final Function<String, Function<WorkflowScreen<?, ?>, Coordinator>> coordinators;

  static Builder newBuilder() {
    return new Builder();
  }

  static class Builder {
    private Map<String, View> views = new LinkedHashMap<>();
    private Map<String, String> bindings = new LinkedHashMap<>();
    private Map<String, Function<WorkflowScreen<?, ?>, Coordinator>> coordinators = new LinkedHashMap<>();

    public Builder view(View view) {
      views.put(view.id, view);
      return this;
    }

    Builder bind(String screenKey, String viewId, Function<WorkflowScreen<?, ?>, Coordinator> coordinatorProvider) {
      bindings.put(screenKey, viewId);
      coordinators.put(screenKey, coordinatorProvider);
      return this;
    }

    ViewFactory build() {
      return new ViewFactory(this);
    }
  }

  private ViewFactory(Builder builder) {
    this.views = builder.views::get;
    this.bindings = builder.bindings::get;
    this.coordinators = builder.coordinators::get;
  }

  @Override public View apply(WorkflowScreen<?, ?> screen) {
    Coordinator coordinator = coordinatorFor(screen);
    return viewFor(screen).attachmentListeners(coordinator::attach, coordinator::detach);
  }

  private View viewFor(WorkflowScreen<?, ?> screen) {
    return views.apply(bindings.apply(screen.key));
  }

  private Coordinator coordinatorFor(WorkflowScreen<?, ?> screen) {
    return coordinators.apply(screen.key).apply(screen);
  }
}
