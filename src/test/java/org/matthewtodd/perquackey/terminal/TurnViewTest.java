package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.table.TableModel;
import org.junit.Test;
import org.matthewtodd.terminal.ViewTester;

public class TurnViewTest {
  @Test public void hookup() {
    ViewTester<TurnView> tester = new ViewTester<>(new TurnView());

    tester.update(view -> {
      view.score.setText("1900 points");
      view.timer.setText("1:42");
      view.words.setTableModel(new TableModel<>("3", "4", "5", "6", "7", "8", "9"));
      view.commandLine.setText(":za");
      view.letters.setText("abc");
    });

    tester.assertRows().containsExactly(
        "1:42                                   1900 points",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":za                                            abc"
    );

    tester.update(view -> view.commandLine.takeFocus());
    tester.assertCursorBufferPosition().isEqualTo(new TerminalPosition(3, 9));
    tester.assertCursorVisible().isTrue();
  }
}
