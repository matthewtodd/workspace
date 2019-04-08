package org.matthewtodd.workflow;

import io.reactivex.processors.BehaviorProcessor;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import static org.assertj.core.api.Assertions.assertThat;

public class StubWorkflow<I, R> implements Workflow<I, R>, StubWorkflowScreen.Events {
  private final AtomicReference<I> input = new AtomicReference<>();
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen = BehaviorProcessor.create();
  private final Processor<R, R> result = BehaviorProcessor.create();

  @Override public void start(I input) {
    this.input.set(input);
    this.screen.onNext(new StubWorkflowScreen(BehaviorProcessor.create(), this));
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<R> result() {
    return result;
  }

  @Override public void assertInputIsEqualTo(Object input) {
    assertThat(this.input.get()).isEqualTo(input);
  }

  @SuppressWarnings("unchecked")
  @Override public void result(Object result) {
    this.screen.onComplete();
    this.result.onNext((R) result);
    this.result.onComplete();
  }
}
