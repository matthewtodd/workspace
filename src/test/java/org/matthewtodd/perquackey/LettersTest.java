package org.matthewtodd.perquackey;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LettersTest {
  @Test public void empty() {
    Letters letters = new Letters();
    assertThat(letters.glean()).isEmpty();
  }

  @Test public void simple() {
    Letters letters = new Letters();
    assertThat(letters.glean("cat")).isEqualTo("act");
  }

  @Test public void reuseAcrossWords() {
    Letters letters = new Letters();
    assertThat(letters.glean("cat", "tack")).isEqualTo("ackt");
  }

  @Test public void reuseWithinWord() {
    Letters letters = new Letters();
    assertThat(letters.glean("attack", "cat")).isEqualTo("aacktt");
  }
}