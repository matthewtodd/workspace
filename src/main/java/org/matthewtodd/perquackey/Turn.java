package org.matthewtodd.perquackey;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Turn {
  private final Timer timer;
  private final AtomicReference<Timer.Snapshot> timerSnapshot;
  private final AtomicReference<WordList> words;
  private final AtomicInteger score;
  private final Processor<Snapshot, Snapshot> snapshot;

  Turn(Timer timer) {
    this.timer = timer;

    timerSnapshot = new AtomicReference<>();
    words = new AtomicReference<>(WordList.EMPTY);
    score = new AtomicInteger(0);
    snapshot = Flow.pipe();

    Flow.of(timer.snapshot()).onComplete(snapshot::onComplete)
        .subscribe(t -> {
          timerSnapshot.set(t);
          snapshot.onNext(takeSnapshot());
        });
  }

  void spell(String word) {
    words.getAndUpdate(w -> w.add(word));
    score.addAndGet(60); // TODO scoring!
    snapshot.onNext(takeSnapshot());
  }

  void startTimer() {
    timer.start();
  }

  void stopTimer() {
    timer.stop();
  }

  Publisher<Snapshot> snapshot() {
    return snapshot;
  }

  private Snapshot takeSnapshot() {
    return new Snapshot(words.get(), score.get(), timerSnapshot.get());
  }

  public static class Snapshot {
    private final WordList words;
    private final int score;
    private final Timer.Snapshot timer;

    Snapshot(WordList words, int score, Timer.Snapshot timer) {
      this.words = words;
      this.score = score;
      this.timer = timer;
    }

    public WordList words() {
      return words;
    }

    public int score() {
      return score;
    }

    public Timer.Snapshot timer() {
      return timer;
    }
  }
}
