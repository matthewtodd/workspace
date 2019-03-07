package org.matthewtodd.perquackey;

import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

import static org.assertj.core.api.Assertions.assertThat;

public class TurnTest {
  // deleting a word / undo
  // auto-remove plural on spelling singular
  // auto-remove singular on spelling plural
  // auto-pluralize when filling category with s available
  @Test public void removesSingularWhenSpellingPlural() {
    AssertSubscriber<WordList> words = AssertSubscriber.create();
    Turn turn = new Turn();
    turn.words().subscribe(words);
    turn.spell("dog");
    assertThat(words.get()).containsExactly("dog");
    turn.spell("dogs");
    assertThat(words.get()).containsExactly("dogs");
  }

  @Test public void removesPluralWhenSpellingSingular() {
    AssertSubscriber<WordList> words = AssertSubscriber.create();
    Turn turn = new Turn();
    turn.words().subscribe(words);
    turn.spell("dogs");
    assertThat(words.get()).containsExactly("dogs");
    turn.spell("dog");
    assertThat(words.get()).containsExactly("dog");
  }
}
