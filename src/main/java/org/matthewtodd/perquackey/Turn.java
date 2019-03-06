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
  private final Input input = new Input();
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

  void letter(char letter) {
    input.append(letter);
    if (input.length() >= 3) {
      input.markValid();
    }
    snapshot.onNext(takeSnapshot());
  }

  void undoLetter() {
    input.chop();
    snapshot.onNext(takeSnapshot());
  }

  void word() {
    if (input.length() < 3) {
      input.markInvalid();
      snapshot.onNext(takeSnapshot());
      return;
    }

    String word = input.value();
    words.add(word);
    input.reset();

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

  void quit() {
    snapshot.onComplete();
  }

  Publisher<Snapshot> snapshot() {
    return snapshot;
  }

  private Snapshot takeSnapshot() {
    return new Snapshot(wordsSnapshot, scoreSnapshot, timerSnapshot, input.snapshot());
  }

  public static class Snapshot {
    private final WordList words;
    private final int score;
    private final Timer.Snapshot timer;
    private Input.Snapshot input;

    Snapshot(WordList words, int score, Timer.Snapshot timer, Input.Snapshot input) {
      this.words = words;
      this.score = score;
      this.timer = timer;
      this.input = input;
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

    public Input.Snapshot input() {
      return input;
    }
  }

  public static class Input {
    private final StringBuilder buffer = new StringBuilder();
    private boolean valid = true;

    void append(char letter) {
      buffer.append(letter);
    }

    void chop() {
      buffer.setLength(Math.max(0, buffer.length() - 1));
    }

    String value() {
      return buffer.toString();
    }

    int length() {
      return buffer.length();
    }

    void markInvalid() {
      valid = false;
    }

    void markValid() {
      valid = true;
    }

    void reset() {
      buffer.setLength(0);
      valid = true;
    }

    Snapshot snapshot() {
      return new Snapshot(value(), valid ? "" : "too short");
    }

    public static class Snapshot {
      private String value;
      private String message;

      Snapshot(String value, String message) {
        this.value = value;
        this.message = message;
      }

      public String value() {
        return value;
      }

      public String message() {
        return message;
      }
    }
  }
}
