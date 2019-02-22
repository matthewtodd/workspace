package org.matthewtodd.perquackey;

import java.util.LinkedHashSet;
import java.util.Set;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Turn {
  private final Set<String> words = new LinkedHashSet<>();
  private final Scorer scorer = new Scorer();
  private final Timer timer;
  private final Processor<Snapshot, Snapshot> snapshot = Flow.pipe();
  private WordList wordsSnapshot = WordList.EMPTY;
  private int scoreSnapshot = 0;
  private Timer.Snapshot timerSnapshot;

  Turn(Timer timer) {
    this.timer = timer;

    Flow.of(timer.snapshot()).subscribe(t -> {
      timerSnapshot = t;
      snapshot.onNext(takeSnapshot());
    });
  }

  void spell(String word) {
    words.add(word);

    if (word.endsWith("s")) {
      String singular = word.substring(0, word.length() - 1);
      words.remove(singular);
    } else {
      String plural = word + "s";
      words.remove(plural);
    }

    wordsSnapshot = new WordList(words);
    scoreSnapshot = scorer.score(wordsSnapshot);
    snapshot.onNext(takeSnapshot());
  }

  void toggleTimer() {
    timer.toggle();
  }

  public void quit() {
    snapshot.onComplete();
  }

  Publisher<Snapshot> snapshot() {
    return snapshot;
  }

  private Snapshot takeSnapshot() {
    return new Snapshot(wordsSnapshot, scoreSnapshot, timerSnapshot);
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
