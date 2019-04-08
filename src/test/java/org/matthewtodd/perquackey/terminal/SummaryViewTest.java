package org.matthewtodd.perquackey.terminal;

import org.junit.Test;
import org.matthewtodd.terminal.ViewTester;

public class SummaryViewTest {
  @Test public void hookup() {
    ViewTester<SummaryView> tester = new ViewTester<>(new SummaryView());

    tester.update(view -> {});

    tester.assertRows().containsExactly(
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        ":                                                 "
    );
  }
}
