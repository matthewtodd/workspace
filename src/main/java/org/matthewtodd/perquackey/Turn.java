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
  private final Input input = new Input();

  private Processor<WordList, WordList> wordPipe = Flow.pipe(WordList.EMPTY);
  private Processor<Integer, Integer> scorePipe = Flow.pipe(0);

  void letter(char letter) {
    input.append(letter);
    if (input.length() >= 3) {
      input.markValid();
    }
  }

  void undoLetter() {
    input.chop();
  }

  void word() {
    if (input.length() < 3) {
      input.markInvalid();
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

    wordPipe.onNext(new WordList(words));
    scorePipe.onNext(scorer.score(words));
  }

  void quit() {
    wordPipe.onComplete();
  }

  Publisher<WordList> words() {
    return wordPipe;
  }

  Publisher<Integer> score() {
    return scorePipe;
  }

  Publisher<Input.State> input() {
    return input.state();
  }
}
