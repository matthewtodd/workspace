package org.matthewtodd.perquackey;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import org.reactivestreams.Publisher;

public class Timer {
  private final long total;
  private final BehaviorProcessor<Boolean> running;
  private final Flowable<Snapshot> snapshot;

  public Timer(long total, Publisher<Long> ticker) {
    this.total = total;
    this.running = BehaviorProcessor.createDefault(false);

    Flowable<Long> elapsed = Flowable.fromPublisher(ticker)
        .map(value -> true) // a steady stream of TRUEs
        .withLatestFrom(running, Boolean::logicalAnd) // falsified when paused
        .filter(Boolean::booleanValue) // a gappy stream of TRUEs
        .map(value -> 1L) // as 1s
        .scan(0L, Long::sum) // cumulatively summed
        .take(total + 1)
        .doOnComplete(running::onComplete);

    this.snapshot = Flowable.combineLatest(elapsed, running, this::newSnapshot);
  }

  void start() {
    running.onNext(true);
  }

  void stop() {
    running.onNext(false);
  }

  Publisher<Snapshot> snapshot() {
    return snapshot;
  }

  private Snapshot newSnapshot(long elapsed, boolean running) {
    return new Snapshot(total, elapsed, running);
  }

  public static final class Snapshot {
    private final long total;
    private final long elapsed;
    private final boolean running;

    Snapshot(long total, long elapsed, boolean running) {
      this.total = total;
      this.elapsed = elapsed;
      this.running = running;
    }

    long remaining() {
      return total - elapsed;
    }

    boolean running() {
      return running;
    }

    @Override public String toString() {
      return String.format("Timer %d/%d [%s]", remaining(), total, running ? "running" : "paused");
    }
  }
}
