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
}
