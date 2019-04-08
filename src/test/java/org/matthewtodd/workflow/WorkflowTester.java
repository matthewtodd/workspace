package org.matthewtodd.workflow;

import java.util.function.BiConsumer;
import org.matthewtodd.flow.AssertSubscriber;

public class WorkflowTester<I, R> {
  private final Workflow<I, R> workflow;
  private final AssertSubscriber<WorkflowScreen<?, ?>> screen;
  private final AssertSubscriber<R> result;

  public WorkflowTester(Workflow<I, R> workflow) {
    this.workflow = workflow;
    this.screen = AssertSubscriber.create("screen");
    this.result = AssertSubscriber.create("result");
    workflow.screen().subscribe(screen);
    workflow.result().subscribe(result);
  }

  public void start(I input) {
    workflow.start(input);
  }

  public <D, E, T extends WorkflowScreen<D, E>> void on(Class<T> screenClass,
      BiConsumer<AssertSubscriber<D>, E> assertions) {
    screen.assertNotComplete();
    T currentScreen = screenClass.cast(screen.get());
    AssertSubscriber<D> data = AssertSubscriber.create("screen data");
    currentScreen.screenData.subscribe(data);
    assertions.accept(data, currentScreen.eventHandler);
  }

  public R result() {
    screen.assertComplete();
    result.assertComplete();
    result.assertValueCount(1);
    return result.get();
  }
}
