package org.matthewtodd.perquackey;

import java.util.Arrays;

public class Scorer {
  static final int[] COLUMN = {0, 0, 0, 50, 100, 150, 200, 350, 500, 500, 0};
  static final int[] WORDS = {0, 0, 0, 10, 20, 50, 100, 150, 250, 500, 1500};
  static final int[] BONUSES = {0, 0, 0, 300, 500, 800, 1200, 1850, 0, 0, 0};

  public int score(String... words) {
    return score(Arrays.asList(words));
  }

  public int score(Iterable<String> words) {
    int[] counts = new int[12]; // one extra to avoid a bounds check in the bonus calculation.
    for (String word : words) {
      counts[word.length()]++;
    }

    int score = 0;
    for (int length = 0; length < counts.length - 1; length++) {
      score += Math.min(counts[length], 1) * COLUMN[length];
      score += Math.min(counts[length], 5) * WORDS[length];

      if (counts[length] >= 5 && counts[length + 1] >= 5) {
        score += BONUSES[length];
      }
    }

    return score;
  }
}
