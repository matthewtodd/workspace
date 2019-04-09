package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;
import java.util.ArrayList;
import java.util.Collection;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.SummaryScreen;
import org.matthewtodd.terminal.Coordinator;

public class SummaryCoordinator implements Coordinator<SummaryView> {
  private final SummaryScreen screen;

  SummaryCoordinator(SummaryScreen screen) {
    this.screen = screen;
  }

  @Override public void attach(SummaryView view) {
    view.commandLine.setKeyPressListener(keyStroke -> {
      if (keyStroke.getKeyType() == KeyType.Character) {
        if (keyStroke.getCharacter() == 'Q') {
          screen.eventHandler.quit();
        }
      }
    });

    Flow.of(screen.screenData).subscribe(summary -> {
      TableModel<Integer> table = view.scores.getTableModel();

      // Update existing columns
      for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
        table.setColumnLabel(columnIndex, summary.playerName(columnIndex));
      }

      // Add new columns
      for (int columnIndex = table.getColumnCount(); columnIndex < summary.playerCount(); columnIndex++) {
        table.addColumn(summary.playerName(columnIndex), null);
      }

      // Update existing rows
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          table.setCell(columnIndex, rowIndex, summary.playerScores(columnIndex).get(rowIndex));
        }
      }

      // Add new rows
      while (table.getRowCount() < summary.scoreCount()) {
        int rowIndex = table.getRowCount();
        Collection<Integer> row = new ArrayList<>(table.getColumnCount());
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          row.add(summary.playerScores(columnIndex).get(rowIndex));
        }
        table.addRow(row);
      }

    });

    view.commandLine.takeFocus();
  }
}
