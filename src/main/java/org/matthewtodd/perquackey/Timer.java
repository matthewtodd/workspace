package org.matthewtodd.perquackey;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class Timer {
  private final long total;
  private final AtomicLong elapsed = new AtomicLong(0);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean done = new AtomicBoolean(false);
  private final Processor<Snapshot, Snapshot> snapshot;

  public Timer(long total, Publisher<Long> ticker) {
    this.total = total;
    this.snapshot = Flow.pipe(takeSnapshot());

    Flow.of(ticker).subscribe(t -> {
      if (!done.get() && running.get()) {
        elapsed.incrementAndGet();
        snapshot.onNext(takeSnapshot());
        if (elapsed.get() == this.total) {
          done.set(true);
          snapshot.onComplete();
        }
      }
    });
  }

  void start() {
    running.set(true);
    snapshot.onNext(takeSnapshot());
  }

  void stop() {
    running.set(false);
    snapshot.onNext(takeSnapshot());
  }

  Publisher<Snapshot> snapshot() {
    return snapshot;
  }

  private Snapshot takeSnapshot() {
    return new Snapshot(total, elapsed.get(), running.get());
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
