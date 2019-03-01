package org.matthewtodd.terminal;

import com.googlecode.lanterna.gui2.Component;
import java.util.function.Consumer;
import java.util.function.Function;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;

public class Application {
  private final Workflow<?, ?> workflow;
  private final Function<WorkflowScreen<?, ?>, Component> viewFactory;
  private final Consumer<Component> ui;

  public Application(Workflow<?, ?> workflow, Function<WorkflowScreen<?, ?>, Component> viewFactory, Consumer<Component> ui) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.ui = ui;
  }

  public void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .as(viewFactory::apply)
        .subscribe(ui);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(_ignored -> {});
  }
}
