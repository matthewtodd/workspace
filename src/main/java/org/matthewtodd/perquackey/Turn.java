package org.matthewtodd.perquackey;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.BehaviorProcessor;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Turn {
  private final Timer timer;
  private final LinkedHashSet<String> currentWords;
  private final BehaviorProcessor<Set<String>> words;
  private final Flowable<Integer> score;
  private final Disposable timerSubscription;

  // Philosophy: things can be built on timers. The turn "lego" depends on a "timer" lego.
  // A turn is inherently timed?
  Turn(Timer timer) {
    this.timer = timer;
    this.currentWords = new LinkedHashSet<>();
    this.words = BehaviorProcessor.createDefault(currentWords);
    this.score = words.map(spelled -> {
      Scorer scorer = new Scorer();
      spelled.forEach(scorer);
      return scorer.score();
    });

    timerSubscription = Completable.fromPublisher(timer.snapshot()).subscribe(this::timeIsUp);
  }

  void spell(String word) {
    currentWords.add(word);
    words.onNext(currentWords);
  }

  void startTimer() {
    timer.start();
  }

  void stopTimer() {
    timer.stop();
  }

  Publisher<Snapshot> snapshot() {
    return Flowable.combineLatest(Snapshot::make, words, score, timer.snapshot());
  }

  private void timeIsUp() {
    words.onComplete();
    timerSubscription.dispose();
  }

  static class Snapshot {
    private final Set<String> words;
    private final int score;
    private final Timer.Snapshot timer;

    @SuppressWarnings("unchecked")
    private static Snapshot make(Object[] values) {
      return new Snapshot((Set<String>) values[0], (int) values[1], (Timer.Snapshot) values[2]);
    }

    private Snapshot(Set<String> words, int score, Timer.Snapshot timer) {
      this.words = Collections.unmodifiableSet(words);
      this.score = score;
      this.timer = timer;
    }

    Set<String> words() {
      return words;
    }

    int score() {
      return score;
    }

    Timer.Snapshot timer() {
      return timer;
    }
  }

  private static class Scorer implements Consumer<String> {
    private int score;

    Scorer() {
      score = 0;
    }

    @Override public void accept(String s) {
      score += 60;
    }

    int score() {
      return score;
    }
  }
}
