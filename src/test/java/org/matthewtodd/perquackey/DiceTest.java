package org.matthewtodd.perquackey;

import org.junit.Before;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DiceTest {
  private Dice dice;
  private AssertSubscriber<String> state;

  @Before public void setUp() {
    state = AssertSubscriber.create();
    dice = new Dice();
    dice.state().subscribe(state);
  }

  @Test public void empty() {
    assertThat(state.get()).isEqualTo("");
  }

  @Test public void simple() {
    dice.observe(singletonList("cat"));
    assertThat(state.get()).isEqualTo("act");
  }

  @Test public void reuseAcrossWords() {
    dice.observe(asList("cat", "tack"));
    assertThat(state.get()).isEqualTo("ackt");
  }

  @Test public void reuseWithinWord() {
    dice.observe(asList("attack", "cat"));
    assertThat(state.get()).isEqualTo("aacktt");
  }
}