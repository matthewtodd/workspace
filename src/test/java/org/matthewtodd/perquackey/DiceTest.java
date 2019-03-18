package org.matthewtodd.perquackey;

import java.util.Arrays;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.junit.Before;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

import static org.assertj.core.api.Assertions.assertThat;

public class DiceTest {
  private DiceTester dice;

  @Before public void setUp() {
    dice = new DiceTester();
  }

  @Test public void canSpell() {
    dice.canSpell("dog");
  }

  @Test public void cannotSpell_impossibleForPerquackeyDice() {
    dice.cannotSpell("quiver");
  }

  @Test public void cannotSpell_impossibleGivenWordsAlreadySeen() {
    dice.canSpell("very");
    dice.observe("quack"); // Q consumes the only available V
    dice.cannotSpell("very");
  }

  @Test public void state() {
    dice.assertThatState().isEqualTo("");
  }

  @Test public void state_simple() {
    dice.observe("cat");
    dice.assertThatState().isEqualTo("act");
  }

  @Test public void state_reuseAcrossWords() {
    dice.observe("cat", "tack");
    dice.assertThatState().isEqualTo("ackt");
  }

  @Test public void state_reuseWithinWords() {
    dice.observe("attack", "cat");
    dice.assertThatState().isEqualTo("aacktt");
  }

  static class DiceTester {
    private final Dice dice;
    private final AssertSubscriber<String> state;

    DiceTester() {
      dice = new Dice();
      state = AssertSubscriber.create();
      dice.state().subscribe(state);
    }

    void canSpell(String word) {
      assertThat(dice.couldSpell(word))
          .describedAs("couldSpell(\"%s\")", word)
          .isTrue();
    }

    void cannotSpell(String word) {
      assertThat(dice.couldSpell(word))
          .describedAs("couldSpell(\"%s\")", word)
          .isFalse();
    }

    void observe(String... words) {
      dice.observe(Arrays.asList(words));
    }

    AbstractCharSequenceAssert<?, String> assertThatState() {
      return assertThat(state.get());
    }
  }
}