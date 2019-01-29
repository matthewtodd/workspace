package org.matthewtodd.flow;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static io.reactivex.Flowable.fromPublisher;

public class Flow {
  public static <T> Builder<T> of(Publisher<T> source) {
    return new Builder<>(fromPublisher(source));
  }

  public static <T> Processor<T, T> pipe() {
    return BehaviorProcessor.create();
  }

  public static <T> Processor<T, T> pipe(T defaultValue) {
    return BehaviorProcessor.createDefault(defaultValue);
  }

  public static final class Builder<T> {
    private final Flowable<T> source;

    private Builder(Flowable<T> source) {
      this.source = source;
    }

    public <T2> Builder<T2> as(Function<T, T2> mapper) {
      return new Builder<>(source.map(mapper::apply));
    }

    public Builder<T> distinct() {
      return new Builder<>(source.distinctUntilChanged());
    }

    public Builder<T> onComplete(Runnable onComplete) {
      return new Builder<>(source.doOnComplete(onComplete::run));
    }

    public Publisher<T> build() {
      return source;
    }

    public Publisher<T> last() {
      return source.lastOrError().toFlowable();
    }

    public void subscribe(Consumer<T> onNext) {
      source.subscribe(new Subscriber<T>() {
        @Override public void onSubscribe(Subscription s) {
          s.request(Long.MAX_VALUE);
        }

        @Override public void onNext(T t) {
          onNext.accept(t);
        }

        @Override public void onError(Throwable t) { }

        @Override public void onComplete() { }
      });
    }
  }

  public static Scheduler newScheduler() {
    return new Scheduler();
  }

  public static final class Scheduler implements Consumer<Runnable> {
    private final BlockingScheduler scheduler = new BlockingScheduler();

    private Scheduler() { }

    // TODO is there an existing something like Supplier<T> that allows throwing an exception?
    public interface InputSource<T> {
      T get() throws IOException;
    }

    public <T> Publisher<T> input(InputSource<T> source) {
      return Flowable.<T>generate(emitter -> emitter.onNext(source.get()))
          .subscribeOn(Schedulers.io())
          .observeOn(scheduler);
    }

    public Publisher<Long> ticking() {
      return Flowable.interval(1, TimeUnit.SECONDS).observeOn(scheduler);
    }

    // TODO make this more reactive?
    // really what we want is to call this after anything happens in the scheduler?
    // so, really, we'd just be wrapping every task submitted.
    public void afterEach(Runnable afterEach) {
      scheduler.schedulePeriodicallyDirect(afterEach, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void start() {
      scheduler.execute();
    }

    @Override public void accept(Runnable action) {
      scheduler.scheduleDirect(action);
    }

    public void shutdown() {
      scheduler.shutdown();
    }
  }
}
