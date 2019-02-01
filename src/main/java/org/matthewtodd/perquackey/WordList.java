package org.matthewtodd.perquackey;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WordList implements Iterable<String> {
  static final WordList EMPTY = new WordList(Collections.emptySet());

  private final Set<String> words;
  private final Map<Integer, List<String>> indexedWords;
  private final int rowCount;

  private WordList(Set<String> words) {
    this.words = Collections.unmodifiableSet(words);
    indexedWords = words.stream().collect(Collectors.groupingBy(String::length));
    rowCount = indexedWords.values().stream().mapToInt(Collection::size).max().orElse(0);
  }

  WordList add(String word) {
    LinkedHashSet<String> newWords = new LinkedHashSet<>(words);
    newWords.add(word);
    return new WordList(newWords);
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
}
