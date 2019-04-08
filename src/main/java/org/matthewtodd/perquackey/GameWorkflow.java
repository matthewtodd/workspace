package org.matthewtodd.perquackey;

import java.util.function.Supplier;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class GameWorkflow implements Workflow<Void, String> {
  private final Processor<String, String> result = Flow.pipe();
  private final TurnWorkflow turnWorkflow;

  public GameWorkflow(Supplier<TurnWorkflow> turnWorkflowSupplier) {
    turnWorkflow = turnWorkflowSupplier.get();
    Flow.of(turnWorkflow.result()).as(state -> "foo").build().subscribe(result);
  }

  @Override public void start(Void input) {
    turnWorkflow.start(false);
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return turnWorkflow.screen();
  }

  @Override public Publisher<String> result() {
    return result;
  }
}
