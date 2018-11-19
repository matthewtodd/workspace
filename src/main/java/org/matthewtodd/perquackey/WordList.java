package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

public class WordList implements Iterable<String> {
  static final WordList EMPTY = new WordList(Collections.emptySet());

  private final Set<String> words;

  private WordList(Set<String> words) {
    this.words = Collections.unmodifiableSet(words);
  }

  WordList add(String word) {
    LinkedHashSet<String> newWords = new LinkedHashSet<>(words);
    newWords.add(word);
    return new WordList(newWords);
  }

  public Map<String, Map<String, String>> asMap() {
    // TODO this is terrible
    return IntStream.rangeClosed(3, 9).collect(LinkedHashMap::new,
        (columns, length) -> columns.put(
            format("words-%d", length),
            words.stream()
                .filter(word -> word.length() == length)
                .collect(() -> new LinkedHashMap<>(singletonMap(format("words-%d-header", length), format("%d", length))),
                    (rows, word) -> rows.put(
                        format("word-%s", word),
                        word
                    ),
                    (a, b) -> { throw new IllegalStateException(); }
                )),
        (a, b) -> { throw new IllegalStateException(); });
  }

  @Override public Iterator<String> iterator() {
    return words.iterator();
  }
}
