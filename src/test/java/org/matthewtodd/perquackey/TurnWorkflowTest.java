package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import org.junit.Test;
import org.matthewtodd.workflow.WorkflowTester;
import org.reactivestreams.Publisher;

public class TurnWorkflowTest {
  @Test public void playingATurn() {
    SynchronousTimer timer = new SynchronousTimer(180);

    WorkflowTester<Void, Turn.Snapshot> workflow = new WorkflowTester<>(new TurnWorkflow(timer));
    workflow.start(null);

    workflow.on(SpellingScreen.class, screen -> {
      screen.assertThat(Turn.Snapshot::words).isEmpty();
      screen.assertThat(Turn.Snapshot::score).isEqualTo(0);
      screen.assertThat(Turn.Snapshot::timer).extracting(Timer.Snapshot::running).containsExactly(true);
      screen.send().spell("dog");
      screen.assertThat(Turn.Snapshot::words).containsExactly("dog");
      screen.assertThat(Turn.Snapshot::score).isEqualTo(60);
      screen.send().pauseTimer();
    });

    workflow.on(PausedScreen.class, screen -> {
      screen.assertThat(Turn.Snapshot::timer).extracting(Timer.Snapshot::running).containsExactly(false);
      screen.send().resumeTimer();
    });

    timer.tickDownToZero();
    workflow.assertThat(Turn.Snapshot::score).isEqualTo(60);
  }

  // TODO I can imagine a timer as a combination of a stream of ticks and a stream of pause/resume events.
  // If built this way, we could re-use most of the production code here, wrapping
  // Flowable.interval(1s) there and a manually-fed BehaviorProcessor here.
  // Poring over the docs, I don't see anything immediately, but it looks like there might be
  // something in the extensions library,
  // https://medium.com/@scottalancooper/pausing-and-resuming-a-stream-in-rxjava-988a0977b771
  // Note that that implementation's not quite what I want (I don't care to buffer and publish events).
  // Anyway, maybe I'll make this better sometime.
  private static class SynchronousTimer implements Timer {
    private final BehaviorProcessor<Snapshot> snapshot;
    private final int total;
    private boolean running;
    private int remaining;

    private SynchronousTimer(int seconds) {
      remaining = total = seconds;
      running = false;
      snapshot = BehaviorProcessor.createDefault(takeSnapshot());
    }

    @Override public void start() {
      assert !running;
      running = true;
      publishSnapshot();
    }

    @Override public void stop() {
      assert running;
      running = false;
      publishSnapshot();
    }

    @Override public Publisher<Snapshot> snapshot() {
      return snapshot;
    }

    void tickDownToZero() {
      assert running;
      while (remaining > 0) {
        remaining--;
        publishSnapshot();
      }
      snapshot.onComplete();
    }

    private void publishSnapshot() {
      snapshot.onNext(takeSnapshot());
    }

    private Snapshot takeSnapshot() {
      return new Snapshot(running, remaining, total);
    }
  }
}
