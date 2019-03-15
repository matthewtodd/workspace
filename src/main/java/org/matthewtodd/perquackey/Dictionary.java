package org.matthewtodd.perquackey;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class Dictionary {
  private final Set<String> words = new LinkedHashSet<>();

  Dictionary(Reader source) {
    new BufferedReader(source).lines()
        .filter(word -> word.length() <= 13)
        .forEach(words::add);
  }

  public static Dictionary standard() {
    return new Dictionary(new InputStreamReader(Dictionary.class.getResourceAsStream("TWL06.txt")));
  }

  boolean contains(String word) {
    return words.contains(word);
  }

  public Map<Integer, Integer> stats() {
    return words.stream().collect(Collectors.toMap(String::length, w -> 1, Integer::sum));
  }

  public int memory() {
    return words.stream().mapToInt(String::length).sum();
  }
}
