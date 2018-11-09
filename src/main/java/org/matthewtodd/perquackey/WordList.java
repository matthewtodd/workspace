package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

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

  public void eachColumn(Consumer<Column> consumer) {
    consumer.accept(new Column(3));
    consumer.accept(new Column(4));
    consumer.accept(new Column(5));
    consumer.accept(new Column(6));
    consumer.accept(new Column(7));
    consumer.accept(new Column(8));
    consumer.accept(new Column(9));
  }

  @Override public Iterator<String> iterator() {
    return words.iterator();
  }

  public class Column {
    private final int length;

    private Column(int length) {
      this.length = length;
    }

    public int length() {
      return length;
    }
  }
}
