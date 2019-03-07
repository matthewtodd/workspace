package org.matthewtodd.perquackey;

import java.util.LinkedHashSet;
import java.util.Set;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Turn {
  private final Set<String> words = new LinkedHashSet<>();
  private final Processor<WordList, WordList> state = Flow.pipe(WordList.EMPTY);

  void spell(String word) {
    words.add(word);

    if (word.endsWith("s")) {
      String singular = word.substring(0, word.length() - 1);
      words.remove(singular);
    } else {
      String plural = word + "s";
      words.remove(plural);
    }

    state.onNext(new WordList(words));
  }

  Publisher<WordList> words() {
    return state;
  }

  void quit() {
    state.onComplete();
  }
}
