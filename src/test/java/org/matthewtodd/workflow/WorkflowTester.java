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
  private final TestSubscriber<? extends WorkflowScreenTester<?, ?>> screen;
  private final TestSubscriber<R> result;

  public WorkflowTester(Workflow<I, R> workflow) {
    this.workflow = workflow;
    this.screen = Flowable.fromPublisher(workflow.screen()).map(WorkflowScreenTester::create).test();
    this.result = Flowable.fromPublisher(workflow.result()).test();
  }

  public void start(I input) {
    workflow.start(input);
  }

  public <D, E, T extends WorkflowScreen<D, E>> void on(Class<T> screenClass,
      Consumer<WorkflowScreenTester<D, E>> assertions) {
    screen.assertNotComplete();
    assertions.accept(currentValue(screen).cast(screenClass));
  }

  public <T> ObjectAssert<T> assertThat(Function<R, T> property) {
    screen.assertComplete();
    result.assertComplete();
    result.assertValueCount(1);
    return new ObjectAssert<>(property.apply(currentValue(result)));
  }

  public static class WorkflowScreenTester<D, E> {
    private final WorkflowScreen<D, E> screen;
    private final TestSubscriber<D> screenData;

    static <D, E> WorkflowScreenTester<D, E> create(WorkflowScreen<D, E> screen) {
      return new WorkflowScreenTester<>(screen);
    }

    private WorkflowScreenTester(WorkflowScreen<D, E> screen) {
      this.screen = screen;
      this.screenData = Flowable.fromPublisher(screen.screenData).test();
    }

    @SuppressWarnings("unchecked")
    <D2, E2, T2 extends WorkflowScreen<D2, E2>> WorkflowScreenTester<D2, E2> cast(Class<T2> screenClass) {
      if (!screenClass.isInstance(screen)) {
        throw new AssertionError(
            String.format("Expected to be on %s, but got %s.", screenClass.getSimpleName(),
                screen.getClass().getSimpleName()));
      }

      return (WorkflowScreenTester<D2, E2>) this;
    }

    public E send() {
      return screen.eventHandler;
    }

    public IntegerAssert assertThat(ToIntFunction<D> property) {
      return new IntegerAssert(property.applyAsInt(currentValue(screenData)));
    }

    public <T> IterableAssert<T> assertThat(ToIterableFunction<D, T> property) {
      return new IterableAssert<>(property.apply(currentValue(screenData)));
    }

    public <T> ObjectAssert<T> assertThat(Function<D, T> property) {
      return new ObjectAssert<>(property.apply(currentValue(screenData)));
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
