package org.matthewtodd.perquackey;

import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

import static org.assertj.core.api.Assertions.assertThat;

public class WordsTest {
  // deleting a word / undo
  // auto-remove plural on spelling singular
  // auto-remove singular on spelling plural
  // auto-pluralize when filling category with s available
  @Test public void removesSingularWhenSpellingPlural() {
    AssertSubscriber<Words.State> words = AssertSubscriber.create();
    Words turn = new Words();
    turn.state().subscribe(words);
    turn.spell("dog");
    assertThat(words.get()).containsExactly("dog");
    turn.spell("dogs");
    assertThat(words.get()).containsExactly("dogs");
  }

  @Test public void removesPluralWhenSpellingSingular() {
    AssertSubscriber<Words.State> words = AssertSubscriber.create();
    Words turn = new Words();
    turn.state().subscribe(words);
    turn.spell("dogs");
    assertThat(words.get()).containsExactly("dogs");
    turn.spell("dog");
    assertThat(words.get()).containsExactly("dog");
  }

  @Test public void lengthOkay() {
    Words words = new Words();
    words.setVulnerable(false);
    assertThat(words.lengthOkay("do")).isFalse();
    assertThat(words.lengthOkay("dog")).isTrue();
    assertThat(words.lengthOkay("dogcatcher")).isTrue();
    assertThat(words.lengthOkay("dogcatchers")).isFalse();
  }

  @Test public void lengthOkay_vulnerable() {
    Words words = new Words();
    words.setVulnerable(true);
    assertThat(words.lengthOkay("dog")).isFalse();
    assertThat(words.lengthOkay("dogs")).isTrue();
    assertThat(words.lengthOkay("dogmatization")).isTrue();
    assertThat(words.lengthOkay("dogmaticalness")).isFalse();
  }

  @Test public void stateColunns() {
    AssertSubscriber<Words.State> state = AssertSubscriber.create();
    Words words = new Words();
    words.setVulnerable(false);
    words.state().subscribe(state);
    assertThat(state.get().columnLabel(0)).isEqualTo("3");
  }

  @Test public void stateColunns_vulnerable() {
    AssertSubscriber<Words.State> state = AssertSubscriber.create();
    Words words = new Words();
    words.setVulnerable(true);
    words.state().subscribe(state);
    assertThat(state.get().columnLabel(0)).isEqualTo("4");
  }
}
