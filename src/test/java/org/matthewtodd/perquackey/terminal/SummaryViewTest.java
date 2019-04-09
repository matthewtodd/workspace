package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.table.TableModel;
import org.junit.Test;
import org.matthewtodd.terminal.ViewTester;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;

public class SummaryViewTest {
  @Test public void hookup() {
    ViewTester<SummaryView> tester = new ViewTester<>(new SummaryView());
    tester.update(view -> {
      view.scores.setTableModel(new TableModel<>("Matthew", "Kathy"));
      view.scores.getTableModel().addRow(3000, 850);
      view.scores.getTableModel().addRow(4900, 1200);
      view.scores.getTableModel().addRow(2600, null);
      view.commandLine.takeFocus();
    });

    tester.assertRows().containsExactly(
        "Matthew Kathy                                     ",
        "   3000    850                                    ",
        "   4900   1200                                    ",
        "------- ------                                    ",
        "   7900   2050                                    ",
        "   2600                                           ",
        "-------                                           ",
        "  10500                                           ",
        "                                                  ",
        ":                                                 "
    );

    tester.assertCursorBufferPosition().isEqualTo(TOP_LEFT_CORNER.withRow(9).withColumn(1));
    tester.assertCursorVisible().isTrue();
  }
}
