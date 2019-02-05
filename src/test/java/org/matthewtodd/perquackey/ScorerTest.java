package org.matthewtodd.perquackey;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScorerTest {
  private final Scorer scorer = new Scorer();

  @Test public void nothing() {
    assertThat(scorer.score()).isEqualTo(0);
  }

  @Test public void threes() {
    assertThat(scorer.score("ape")).isEqualTo(60);
    assertThat(scorer.score("ape", "bar")).isEqualTo(70);
    assertThat(scorer.score("ape", "bar", "cat")).isEqualTo(80);
    assertThat(scorer.score("ape", "bar", "cat", "dog")).isEqualTo(90);
    assertThat(scorer.score("ape", "bar", "cat", "dog", "eye")).isEqualTo(100);
    assertThat(scorer.score("ape", "bar", "cat", "dog", "eye", "fan")).isEqualTo(100);
  }

  @Test public void fours() {
    assertThat(scorer.score("aped")).isEqualTo(120);
    assertThat(scorer.score("aped", "bare")).isEqualTo(140);
    assertThat(scorer.score("aped", "bare", "cate")).isEqualTo(160);
    assertThat(scorer.score("aped", "bare", "cate", "doge")).isEqualTo(180);
    assertThat(scorer.score("aped", "bare", "cate", "doge", "eyed")).isEqualTo(200);
    assertThat(scorer.score("aped", "bare", "cate", "doge", "eyed", "fang")).isEqualTo(200);
  }

  @Test public void bonuses() {
    assertThat(scorer.score(
        "ape", "aped",
        "bar", "bare",
        "cat", "cate",
        "dog", "doge",
        "eye", "eyed")).isEqualTo(600);

    assertThat(scorer.score(
        "abhorrent", "avaricious",
        "bunkhouse", "burglarize",
        "cyclorama", "condensate",
        "dysphoric", "duplicable",
        "exultance", "eyewitness")).isEqualTo(10500);
  }
}
