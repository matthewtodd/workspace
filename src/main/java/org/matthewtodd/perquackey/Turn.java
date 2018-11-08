package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Turn {
  private final Timer timer;
  private final AtomicReference<Timer.Snapshot> timerSnapshot;
  private final Set<String> words;
  private final AtomicInteger score;
  private final Processor<Snapshot, Snapshot> snapshot;

  Turn(Timer timer) {
    this.timer = timer;

    timerSnapshot = new AtomicReference<>();
    words = new LinkedHashSet<>();
    score = new AtomicInteger(0);
    snapshot = Flow.pipe();

    Flow.of(timer.snapshot()).onComplete(snapshot::onComplete)
        .subscribe(t -> {
          timerSnapshot.set(t);
          snapshot.onNext(takeSnapshot());
        });
  }

  void spell(String word) {
    words.add(word);
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
    return new Snapshot(words, score.get(), timerSnapshot.get());
  }

  public static class Snapshot {
    private final Set<String> words;
    private final int score;
    private final Timer.Snapshot timer;

    private Snapshot(Set<String> words, int score, Timer.Snapshot timer) {
      this.words = Collections.unmodifiableSet(words);
      this.score = score;
      this.timer = timer;
    }

    public Set<String> words() {
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
