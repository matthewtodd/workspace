package org.matthewtodd.perquackey;

import java.util.Arrays;
import java.util.stream.StreamSupport;

class Letters {
  public String glean(String... words) {
    return glean(Arrays.asList(words));
  }

  public String glean(Iterable<String> words) {
    byte[] tally = StreamSupport.stream(words.spliterator(), false)
        .map(Letters::tally)
        .reduce(new byte[26], Letters::max);

    StringBuilder result = new StringBuilder();
    for (char letter = 'a'; letter <= 'z'; letter++) {
      for (int i = 0; i < tally[letter - 'a']; i++) {
        result.append(letter);
      }
    }
    return result.toString();
  }

  private static byte[] tally(String word) {
    byte[] tally = new byte[26];
    word.chars().forEach(c -> tally[c - 'a']++);
    return tally;
  }

  private static byte[] max(byte[] a, byte[] b) {
    byte[] result = new byte[26];
    for (char letter = 'a'; letter <= 'z'; letter++) {
      result[letter - 'a'] = (byte) Math.max(a[letter - 'a'], b[letter - 'a']);
    }
    return result;
  }
}
