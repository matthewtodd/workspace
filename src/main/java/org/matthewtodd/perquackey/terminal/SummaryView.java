package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableCellRenderer;
import com.googlecode.lanterna.gui2.table.TableHeaderRenderer;
import org.matthewtodd.terminal.CommandLine;
import org.matthewtodd.terminal.View;

import static com.googlecode.lanterna.TerminalPosition.TOP_LEFT_CORNER;

class SummaryView extends View<SummaryView> {
  final Table<Integer> scores = new Table<Integer>("")
      .setTableHeaderRenderer(new ScoreTableHeaderRenderer())
      .setTableCellRenderer(new ScoreTableCellRenderer());

  final CommandLine commandLine = new CommandLine();

  SummaryView() {
    setComponent(new Panel(new BorderLayout())
        .addComponent(new Panel(new BorderLayout())
            .addComponent(scores)
            .setLayoutData(BorderLayout.Location.CENTER))
        .addComponent(new Panel(new GridLayout(1).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(commandLine, GridLayout.createHorizontallyFilledLayoutData(1))
            .setLayoutData(BorderLayout.Location.BOTTOM)));
  }

  private static class ScoreTableHeaderRenderer implements TableHeaderRenderer<Integer> {
    @Override public TerminalSize getPreferredSize(Table<Integer> table, String label, int columnIndex) {
      return TerminalSize.ONE.withColumns(Math.max(label.length(), 6));
    }

    @Override public void drawHeader(Table<Integer> table, String label, int index, TextGUIGraphics textGUIGraphics) {
      textGUIGraphics.putString(TOP_LEFT_CORNER, label);
    }
  }

  private static class ScoreTableCellRenderer implements TableCellRenderer<Integer> {
    @Override public TerminalSize getPreferredSize(Table<Integer> table, Integer cell, int columnIndex, int rowIndex) {
      return new TerminalSize(6, rowIndex == 0 ? 1 : 3);
    }

    @Override public void drawCell(Table<Integer> table, Integer cell, int columnIndex, int rowIndex, TextGUIGraphics gui) {
      int columns = gui.getSize().getColumns();

      String turnScore = String.valueOf(cell);
      gui.putString(new TerminalPosition(columns - turnScore.length(), 0), turnScore);

      if (rowIndex > 0) {
        gui.putString(new TerminalPosition(0, 1), "--------------------".substring(0, columns));

        int cumulativeScoreValue = 0;
        for (int i = 0; i <= rowIndex; i++) {
          cumulativeScoreValue += table.getTableModel().getCell(columnIndex, i);
        }

        String cumulativeScore = String.valueOf(cumulativeScoreValue);
        gui.putString(new TerminalPosition(columns - cumulativeScore.length(), 2), cumulativeScore);
      }
    }
  }
}
