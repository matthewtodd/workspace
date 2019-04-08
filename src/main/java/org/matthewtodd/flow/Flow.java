package org.matthewtodd.flow;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static io.reactivex.Flowable.combineLatest;
import static io.reactivex.Flowable.fromPublisher;

public class Flow {
  public static <T> Builder<T> of(Publisher<T> source) {
    return new Builder<>(fromPublisher(source));
  }

  public static <T1, T2, T3, T4, T5> Builder5<T1, T2, T3, T4, T5> of(Publisher<T1> source1,
      Publisher<T2> source2, Publisher<T3> source3, Publisher<T4> source4, Publisher<T5> source5) {
    return new Builder5<>(fromPublisher(source1), fromPublisher(source2), fromPublisher(source3),
        fromPublisher(source4), fromPublisher(source5));
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

    public Builder<T> onComplete(Runnable onComplete) {
      return new Builder<>(source.doOnComplete(onComplete::run));
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

        @Override public void onError(Throwable t) { throw new RuntimeException(t); }

        @Override public void onComplete() { }
      });
    }

    public Publisher<T> build() {
      return source;
    }
  }

  public static final class Builder5<T1, T2, T3, T4, T5> {
    private final Flowable<T1> source1;
    private final Flowable<T2> source2;
    private final Flowable<T3> source3;
    private final Flowable<T4> source4;
    private final Flowable<T5> source5;

    private Builder5(Flowable<T1> source1, Flowable<T2> source2, Flowable<T3> source3,
        Flowable<T4> source4, Flowable<T5> source5) {
      this.source1 = source1;
      this.source2 = source2;
      this.source3 = source3;
      this.source4 = source4;
      this.source5 = source5;
    }

    public <T> Builder<T> as(Function5<T1, T2, T3, T4, T5, T> combiner) {
      return new Builder<>(
          combineLatest(source1, source2, source3, source4, source5, combiner::apply));
    }

    public interface Function5<T1, T2, T3, T4, T5, T> {
      T apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }
  }

  public static Scheduler newScheduler() {
    return new Scheduler();
  }

  public static class Scheduler {
    private final BlockingScheduler scheduler;

    private Scheduler() {
      scheduler = new BlockingScheduler();
    }

    public void loop(Runnable task) {
      scheduler.schedulePeriodicallyDirect(task, 0, 1, TimeUnit.MILLISECONDS);
    }

    public Publisher<Long> ticking() {
      return Flowable.interval(1, TimeUnit.SECONDS).observeOn(scheduler);
    }

    public void start() {
      scheduler.execute();
    }

    public void shutdown() {
      scheduler.shutdown();
    }
  }
}
