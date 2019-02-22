package org.matthewtodd.perquackey;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Thoughts?
// - This group words into columns by length functionality is exactly what the scorer does.
//   So, the scorer could use this class; or we could revert this to a simple collection.
public class WordList implements Iterable<String> {
  static final WordList EMPTY = new WordList(Collections.emptySet());

  private final Set<String> words;
  private final Map<Integer, List<String>> indexedWords;
  private final int rowCount;

  WordList(Set<String> words) {
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
