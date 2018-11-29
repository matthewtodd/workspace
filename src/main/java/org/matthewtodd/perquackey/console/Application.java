package org.matthewtodd.perquackey.console;

import org.matthewtodd.console.Window;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;

class Application {
  private final ViewFactory viewFactory;
  private final Window window;
  private final Workflow<?, ?> workflow;

  Application(Workflow<?, ?> workflow, ViewFactory viewFactory, Window window) {
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
        .as(viewFactory::get)
        .subscribe(window::rootView);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(turn -> window.close());

    workflow.start(null);
  }
}
