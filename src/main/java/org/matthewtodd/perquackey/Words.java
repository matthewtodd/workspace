package org.matthewtodd.perquackey;

import java.time.temporal.ValueRange;
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
  private ValueRange lengthValidator = ValueRange.of(3, 10);

  void setVulnerable(Boolean vulnerable) {
    lengthValidator = vulnerable ? ValueRange.of(4, 13) : ValueRange.of(3, 10);
    state.onNext(new State(words, (int) lengthValidator.getMinimum()));
  }

  boolean lengthOkay(String word) {
    return lengthValidator.isValidValue(word.length());
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

    state.onNext(new State(words, (int) lengthValidator.getMinimum()));
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
    static final State EMPTY = new State(Collections.emptySet(), 3);

    private final Set<String> words;
    private final Map<Integer, List<String>> indexedWords;
    private final int minimumLength;
    private final int rowCount;

    State(Set<String> words, int minimumLength) {
      this.words = Collections.unmodifiableSet(words);
      this.minimumLength = minimumLength;
      indexedWords = words.stream().collect(Collectors.groupingBy(String::length));
      rowCount = indexedWords.values().stream().mapToInt(Collection::size).max().orElse(0);
    }

    @Override public Iterator<String> iterator() {
      return words.iterator();
    }

    public int columnCount() {
      return 7;
    }

    public String columnLabel(int columnIndex) {
      return String.valueOf(columnIndexToWordLength(columnIndex));
    }

    public int rowCount() {
      return rowCount;
    }

    public String getWord(int columnIndex, int rowIndex) {
      List<String> wordsOfLength =
          indexedWords.getOrDefault(columnIndexToWordLength(columnIndex), Collections.emptyList());
      return rowIndex < wordsOfLength.size() ? wordsOfLength.get(rowIndex) : null;
    }

    @Override public String toString() {
      return words.toString();
    }

    private int columnIndexToWordLength(int columnIndex) {
      return columnIndex + minimumLength;
    }
  }
}
