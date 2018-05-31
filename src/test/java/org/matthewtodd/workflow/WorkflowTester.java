package org.matthewtodd.workflow;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.assertj.core.api.IntegerAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;

public class WorkflowTester<I, R> {
  private final Workflow<I, R> workflow;
  private final TestSubscriber<WorkflowScreenTester<?, ?>> screen;
  private final TestSubscriber<R> result;

  public WorkflowTester(Workflow<I, R> workflow) {
    this.workflow = workflow;
    this.screen = TestSubscriber.create();
    this.result = TestSubscriber.create();
  }

  public void start(I input) {
    Flowable.fromPublisher(workflow.screen())
        .map(s -> new WorkflowScreenTester<>(s))
        .subscribe(screen);
    workflow.result().subscribe(result);
    workflow.start(input);
  }

  public <D, E, T extends WorkflowScreen<D, E>> void on(Class<T> screenClass,
      Consumer<WorkflowScreenTester<D, E>> assertions) {
    // TODO not sure this is giving us type safety?
    assertions.accept(currentValue(screen).cast(screenClass));
  }

 public <T> ObjectAssert<T> assertThat(Function<R, T> property) {
   screen.assertComplete();
   result.assertComplete();
   result.assertValueCount(1);
   return new ObjectAssert<>(property.apply(currentValue(result)));
  }

  public class WorkflowScreenTester<D, E> {
    private final WorkflowScreen<D, E> screen;
    private final TestSubscriber<D> screenData;

    private WorkflowScreenTester(WorkflowScreen<D, E> screen) {
      this.screen = screen;
      screenData = TestSubscriber.create();
      screen.screenData.subscribe(screenData);
    }

    @SuppressWarnings("unchecked")
    private <D2, E2, T2 extends WorkflowScreen<D2, E2>> WorkflowScreenTester<D2, E2> cast(Class<T2> screenClass) {
      if (!screenClass.isInstance(screen)) {
        throw new AssertionError(
            String.format("Expected to be on %s, but got %s.", screenClass.getSimpleName(),
                screen.getClass().getSimpleName()));
      }

      return (WorkflowScreenTester<D2, E2>) this;
    }

    public E send() {
      assertCurrent();
      return screen.eventHandler;
    }

    public IntegerAssert assertThat(ToIntFunction<D> property) {
      assertCurrent();
      return new IntegerAssert(property.applyAsInt(currentValue(screenData)));
    }

    public <T> IterableAssert<T> assertThat(ToIterableFunction<D, T> property) {
      assertCurrent();
      return new IterableAssert<>(property.apply(currentValue(screenData)));
    }

    public <T> ObjectAssert<T> assertThat(Function<D, T> property) {
      assertCurrent();
      return new ObjectAssert<>(property.apply(currentValue(screenData)));
    }

    private void assertCurrent() {
      WorkflowScreenTester<?, ?> currentScreenTester = currentValue(WorkflowTester.this.screen);
      if (this != currentScreenTester) {
        throw new AssertionError("This %s has been replaced by %s");
      }
    }
  }

  public interface ToIterableFunction<T, E> {
    Iterable<E> apply(T value);
  }

  // TODO could move this to a TestSubscribers or something.
  private static <T> T currentValue(TestSubscriber<T> subscriber) {
    return subscriber.values().get(subscriber.valueCount() - 1);
  }
}
