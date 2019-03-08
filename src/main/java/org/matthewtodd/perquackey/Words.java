package org.matthewtodd.perquackey;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

// This class could offer a transformer from event to state.
public class Words {
  private final Set<String> words = new LinkedHashSet<>();
  private final Processor<State, State> state = Flow.pipe(State.EMPTY);

  void spell(String word) {
    words.add(word);

    if (word.endsWith("s")) {
      String singular = word.substring(0, word.length() - 1);
      words.remove(singular);
    } else {
      String plural = word + "s";
      words.remove(plural);
    }

    state.onNext(new State(words));
  }

  Publisher<State> state() {
    return state;
  }

  void quit() {
    state.onComplete();
  }

  // Thoughts?
  // - This group words into columns by length functionality is exactly what the scorer does.
  //   So, the scorer could use this class; or we could revert this to a simple collection.
  public static class State implements Iterable<String> {
    static final State EMPTY = new State(Collections.emptySet());

    private final Set<String> words;
    private final Map<Integer, List<String>> indexedWords;
    private final int rowCount;

    State(Set<String> words) {
      this.words = Collections.unmodifiableSet(words);
      indexedWords = words.stream().collect(Collectors.groupingBy(String::length));
      rowCount = indexedWords.values().stream().mapToInt(Collection::size).max().orElse(0);
    }

    @Override public Iterator<String> iterator() {
      return words.iterator();
    }

    public int rowCount() {
      return rowCount;
    }

    public String getWord(int length, int index) {
      List<String> wordsOfLength = indexedWords.getOrDefault(length, Collections.emptyList());
      return index < wordsOfLength.size() ? wordsOfLength.get(index) : null;
    }

    @Override public String toString() {
      return words.toString();
    }
  }
}
