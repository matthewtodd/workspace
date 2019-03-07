package org.matthewtodd.perquackey;

import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

public class InputTest {
  @Test public void rejectsWordsThatAreTooShort() {
    AssertSubscriber<String> words = AssertSubscriber.create();
    Input input = new Input();
    input.words().subscribe(words);
    input.letter('z');
    input.letter('a');
    input.word();
    words.assertEmpty();
  }
}
