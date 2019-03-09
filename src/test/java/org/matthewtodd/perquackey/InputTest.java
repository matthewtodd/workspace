package org.matthewtodd.perquackey;

import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

public class InputTest {
  @Test public void acceptsWordsInTheDictionary() {
    AssertSubscriber<String> entries = AssertSubscriber.create();
    Input input = new Input(word -> true);
    input.entries().subscribe(entries);
    input.letter('z');
    input.letter('a');
    input.letter('p');
    input.enter();
    entries.assertValues("zap");
  }

  @Test public void rejectsWordsThatAreNotInTheDictionary() {
    AssertSubscriber<String> entries = AssertSubscriber.create();
    Input input = new Input(word -> false);
    input.entries().subscribe(entries);
    input.letter('z');
    input.letter('a');
    input.letter('p');
    input.enter();
    entries.assertEmpty();
  }
}
