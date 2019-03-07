package org.matthewtodd.terminal;

import com.googlecode.lanterna.gui2.Component;
import java.util.function.Function;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;

public class Application {
  private final Workflow<?, ?> workflow;
  private final Function<WorkflowScreen<?, ?>, Component> viewFactory;
  private final TerminalUI ui;

  public Application(Workflow<?, ?> workflow, Function<WorkflowScreen<?, ?>, Component> viewFactory, TerminalUI ui) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.ui = ui;
  }

  // Visible for testing.
  public <T extends View> T currentView(Class<T> viewClass) {
    return viewClass.cast(ui.getComponent());
  }

  public void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .as(viewFactory::apply)
        .subscribe(ui);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(_ignored -> ui.close());

    workflow.start(null);
  }
}
