package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

  @Override public Iterator<String> iterator() {
    return words.iterator();
  }
}
