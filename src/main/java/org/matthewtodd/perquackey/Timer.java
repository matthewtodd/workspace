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
  private final Processor<State, State> state;

  Timer(long total) {
    this.total = total;
    this.state = Flow.pipe(snapshotState());
  }

  void tick(long tick) {
    if (!done.get() && running.get()) {
      elapsed.incrementAndGet();
      state.onNext(snapshotState());
      if (elapsed.get() == total) {
        done.set(true);
        state.onComplete();
      }
    }
  }

  void toggle() {
    running.set(!running.get());
    state.onNext(snapshotState());
  }

  Publisher<State> state() {
    return state;
  }

  private State snapshotState() {
    return new State(total - elapsed.get(), running.get());
  }

  public static final class State {
    private final boolean running;
    private long remaining;

    State(long remaining, boolean running) {
      this.remaining = remaining;
      this.running = running;
    }

    public long remaining() {
      return remaining;
    }

    public boolean running() {
      return running;
    }
  }
}
