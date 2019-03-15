package org.matthewtodd.perquackey;

import java.io.StringReader;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DictionaryTest {
  @Test public void emptyDictionaryContainsNothing() {
    assertThat(new Dictionary(new StringReader("")).contains("foo")).isFalse();
  }

  @Test public void readsWordsFromNewlineDelimitedSource() {
    assertThat(new Dictionary(new StringReader("foo\nbar")).contains("foo")).isTrue();
  }

  @Test public void standard() {
    Dictionary dictionary = Dictionary.standard();
    assertThat(dictionary.contains("foo")).isFalse();
    assertThat(dictionary.contains("food")).isTrue();
  }

  @Test public void stats() {
    Map<Integer, Integer> stats = Dictionary.standard().stats();
    assertThat(stats.entrySet()).containsExactly(
        entry(3, 1015),
        entry(4, 4030),
        entry(5, 8938),
        entry(6, 15788),
        entry(7, 24029),
        entry(8, 29766),
        entry(9, 29150),
        entry(10, 22326),
        entry(11, 16165),
        entry(12, 11417),
        entry(13, 7750) //,
        //entry(15, 3157)
    );
  }

  @Test public void memory() {
    // Save 120Kb / 7.5% of memory by stripping 14- and 15-letter words.
    //assertThat(Dictionary.standard().memory()).isEqualTo(1584476);
    assertThat(Dictionary.standard().memory()).isEqualTo(1466093);
  }
}
