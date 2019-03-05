package org.matthewtodd.perquackey;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
  private AtomicReference<Event> eventSnapshot = new AtomicReference<>();

  Turn(Timer timer) {
    this.timer = timer;

    Flow.of(timer.snapshot()).subscribe(t -> {
      timerSnapshot = t;
      snapshot.onNext(takeSnapshot());
    });
  }

  // What do we need?
  // A way to keep a rejected word in the input field?
  // An outgoing message? (Structured! So the UI can interpret it. Could become a more general command.)
  //
  // Dictionary: validation, suggestion
  // Messaging
  // Letters: auto-gathered, added in
  //
  // While-you-type suggestions need a letter-by-letter API...
  void spell(String word) {
    if (word.length() < 3) {
      eventSnapshot.set(Event.REJECTED);
      snapshot.onNext(takeSnapshot());
      return;
    }

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
    eventSnapshot.set(Event.ACCEPTED);
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
    return new Snapshot(wordsSnapshot, scoreSnapshot, timerSnapshot, eventSnapshot.getAndSet(null));
  }

  public static class Snapshot {
    private final WordList words;
    private final int score;
    private final Timer.Snapshot timer;
    private final Event event;

    Snapshot(WordList words, int score, Timer.Snapshot timer,
        Event event) {
      this.words = words;
      this.score = score;
      this.timer = timer;
      this.event = event;
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

    public Event event() {
      return event;
    }
  }

  public enum Event {
    ACCEPTED, REJECTED
  }
}
