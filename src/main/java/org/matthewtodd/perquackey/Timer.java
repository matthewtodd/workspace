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

  // TODO caller should subscribe the timer to the ticker!
  public Timer(long total, Publisher<Long> ticker) {
    this.total = total;
    this.state = Flow.pipe(takeSnapshot());

    Flow.of(ticker).subscribe(t -> {
      if (!done.get() && running.get()) {
        elapsed.incrementAndGet();
        state.onNext(takeSnapshot());
        if (elapsed.get() == this.total) {
          done.set(true);
          state.onComplete();
        }
      }
    });
  }

  void toggle() {
    running.set(!running.get());
    state.onNext(takeSnapshot());
  }

  Publisher<State> state() {
    return state;
  }

  private State takeSnapshot() {
    return new State(total, elapsed.get(), running.get());
  }

  public static final class State {
    private final long total;
    private final long elapsed;
    private final boolean running;

    State(long total, long elapsed, boolean running) {
      this.total = total;
      this.elapsed = elapsed;
      this.running = running;
    }

    public long remaining() {
      return total - elapsed;
    }

    public boolean running() {
      return running;
    }

    @Override public String toString() {
      return String.format("Timer %d/%d [%s]", remaining(), total, running ? "running" : "paused");
    }
  }
}
