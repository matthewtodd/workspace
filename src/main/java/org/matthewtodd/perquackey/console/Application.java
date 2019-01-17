package org.matthewtodd.perquackey.console;

import java.util.function.Function;
import org.matthewtodd.console.View;
import org.matthewtodd.console.Window;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;

class Application {
  private final Workflow<?, ?> workflow;
  private final Function<WorkflowScreen<?, ?>, View> viewFactory;
  private final Window window;

  Application(Workflow<?, ?> workflow, Function<WorkflowScreen<?, ?>, View> viewFactory, Window window) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.window = window;
  }

  void run(Flow.Scheduler mainThread) {
    mainThread.accept(() -> start(mainThread::shutdown));
    mainThread.start();
  }

  void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .as(viewFactory::apply)
        .subscribe(window::rootView);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(turn -> window.close());

    workflow.start(null);
  }
}
