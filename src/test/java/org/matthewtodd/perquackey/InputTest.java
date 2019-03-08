package org.matthewtodd.perquackey;

import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

public class InputTest {
  @Test public void rejectsWordsThatAreTooShort() {
    AssertSubscriber<String> entries = AssertSubscriber.create();
    Input input = new Input();
    input.entries().subscribe(entries);
    input.letter('z');
    input.letter('a');
    input.enter();
    entries.assertEmpty();
  }
}
