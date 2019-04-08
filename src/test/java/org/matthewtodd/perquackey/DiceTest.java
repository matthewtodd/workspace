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
    // Q consumes the only available V
    dice.canSpell("very").observe("quack").cannotSpell("very");
  }

  @Test public void canSpell_vulnerable() {
    // vulnerable dice offer another V
    dice.vulnerable().canSpell("very").observe("quack").canSpell("very");
  }

  @Test public void state() {
    dice.assertThatKnownLetters().isEqualTo("");
  }

  @Test public void state_simple() {
    dice.observe("cat");
    dice.assertThatKnownLetters().isEqualTo("act");
  }

  @Test public void state_reuseAcrossWords() {
    dice.observe("cat", "tack");
    dice.assertThatKnownLetters().isEqualTo("ackt");
  }

  @Test public void state_reuseWithinWords() {
    dice.observe("attack", "cat");
    dice.assertThatKnownLetters().isEqualTo("aacktt");
  }

  static class DiceTester {
    private final Dice dice;
    private final AssertSubscriber<Dice.State> state;

    DiceTester() {
      dice = new Dice();
      state = AssertSubscriber.create("dice state");
      dice.state().subscribe(state);
    }

    DiceTester canSpell(String word) {
      assertThat(dice.couldSpell(word))
          .describedAs("couldSpell(\"%s\")", word)
          .isTrue();
      return this;
    }

    DiceTester cannotSpell(String word) {
      assertThat(dice.couldSpell(word))
          .describedAs("couldSpell(\"%s\")", word)
          .isFalse();
      return this;
    }

    DiceTester observe(String... words) {
      dice.observe(Arrays.asList(words));
      return this;
    }

    AbstractCharSequenceAssert<?, String> assertThatKnownLetters() {
      return assertThat(state.get().known());
    }

    DiceTester vulnerable() {
      dice.setVulnerable(true);
      return this;
    }
  }
}