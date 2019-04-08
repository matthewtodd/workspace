package org.matthewtodd.perquackey;

import java.util.function.Supplier;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class GameWorkflow implements Workflow<Void, String>, SummaryScreen.Events {
  private final Workflow<Boolean, Integer> turnWorkflow;
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen = Flow.pipe();
  private final Processor<String, String> result = Flow.pipe();

  public GameWorkflow(Supplier<Workflow<Boolean, Integer>> turnWorkflowFactory) {
    turnWorkflow = turnWorkflowFactory.get();
  }

  @Override public void start(Void input) {
    Flow.of(turnWorkflow.screen())
        .subscribe(screen::onNext);

    Flow.of(turnWorkflow.result())
        .subscribe(result -> screen.onNext(new SummaryScreen(Flow.pipe(), this)));

    turnWorkflow.start(false);
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<String> result() {
    return result;
  }

  @Override public void quit() {
    screen.onComplete();
    result.onNext("");
    result.onComplete();
  }
}
