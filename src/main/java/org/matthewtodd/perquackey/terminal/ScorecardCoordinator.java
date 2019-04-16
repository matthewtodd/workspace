package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.ScorecardScreen;
import org.matthewtodd.terminal.Coordinator;

public class ScorecardCoordinator implements Coordinator<ScorecardView> {
  private final ScorecardScreen screen;
  private final List<Runnable> cancelHooks;

  ScorecardCoordinator(ScorecardScreen screen) {
    this.screen = screen;
    this.cancelHooks = new ArrayList<>();
  }

  @Override public void attach(ScorecardView view) {
    view.commandLine.setKeyPressListener(keyStroke -> {
      if (keyStroke.getKeyType() == KeyType.Character) {
        if (keyStroke.getCharacter() == 'Q') {
          screen.eventHandler.quit();
        } else if (Character.isDigit(keyStroke.getCharacter())) {
          screen.eventHandler.numberOfPlayers(Integer.parseInt(keyStroke.getCharacter().toString()));
        }
      } else if (keyStroke.getKeyType() == KeyType.Enter) {
        screen.eventHandler.nextTurn();
      }
    });

    Flow.of(screen.screenData).subscribe(scorecard -> {
      TableModel<Integer> table = view.scores.getTableModel();

      // Remove old columns
      for (int columnIndex = scorecard.playerCount(); columnIndex < table.getColumnCount();) {
        table.removeColumn(columnIndex);
      }

      // Update existing columns
      for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
        table.setColumnLabel(columnIndex, scorecard.playerName(columnIndex));
      }

      // Add new columns
      for (int columnIndex = table.getColumnCount(); columnIndex < scorecard.playerCount(); columnIndex++) {
        table.addColumn(scorecard.playerName(columnIndex), null);
      }

      // Update existing rows
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          table.setCell(columnIndex, rowIndex, scorecard.playerScore(columnIndex, rowIndex));
        }
      }

      // Add new rows
      while (table.getRowCount() < scorecard.scoreCount()) {
        int rowIndex = table.getRowCount();
        Collection<Integer> row = new ArrayList<>(table.getColumnCount());
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          row.add(scorecard.playerScore(columnIndex, rowIndex));
        }
        table.addRow(row);
      }
    }, cancelHooks::add);

    view.commandLine.takeFocus();
  }

  @Override public void detach(ScorecardView component) {
    cancelHooks.forEach(Runnable::run);
    cancelHooks.clear();
  }
}
