package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.TurnScreen;
import org.matthewtodd.perquackey.Words;
import org.matthewtodd.terminal.Coordinator;

class TurnCoordinator implements Coordinator<TurnView> {
  private final TurnScreen screen;

  TurnCoordinator(TurnScreen screen) {
    this.screen = screen;
  }

  @Override public void attach(TurnView view) {
    view.commandLine.setKeyPressListener(keyStroke -> {
      switch (keyStroke.getKeyType()) {
        case Character:
          switch (keyStroke.getCharacter()) {
            case ' ':
              screen.eventHandler.toggleTimer();
              break;
            case 'Q':
              screen.eventHandler.quit();
              break;
            default:
              screen.eventHandler.letter(keyStroke.getCharacter());
              break;
          }
          break;
        case Backspace:
          screen.eventHandler.undoLetter();
          break;
        case Enter:
          screen.eventHandler.word();
          break;
      }
    });

    Flow.of(screen.screenData).subscribe(turn -> {
      // TODO could push formatting up into the view widgets.
      view.score.setText(String.format("%d points", turn.score()));

      view.timer.setText(String.format("%d:%02d%s",
          turn.timer().remaining() / 60,
          turn.timer().remaining() % 60,
          turn.timer().running() ? "" : " [paused]"));

      new TableUpdater(view.words.getTableModel()).update(turn.words());

      view.commandLine.setText(turn.input());
      view.letters.setText(String.format("%s:%S", turn.knownLetters(), turn.unknownLetters()));
    });

    view.commandLine.takeFocus();
  }

  static class TableUpdater {
    private final TableModel<String> table;

    TableUpdater(TableModel<String> tableModel) {
      table = tableModel;
    }

    // TODO rather than passing this object, can we pass access to the data we need?
    // so, words::rowCount as a Supplier<Integer>, for example.
    // But that's premature because I'm not sure how to think about the column headers being so overloaded.
    void update(Words.State words) {
      for (int i = 0; i < table.getColumnCount(); i++) {
        table.setColumnLabel(i, words.columnLabel(i));
      }

      for (int i = table.getColumnCount(); i < words.columnCount(); i++) {
        table.addColumn(words.columnLabel(i), null);
      }

      // update existing rows
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          table.setCell(columnIndex, rowIndex, words.getWord(columnIndex, rowIndex));
        }
      }

      // add new rows
      while (table.getRowCount() < words.rowCount()) {
        int rowIndex = table.getRowCount();
        Collection<String> row = new ArrayList<>(table.getColumnCount());
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          row.add(words.getWord(columnIndex, rowIndex));
        }
        table.addRow(row);
      }

      // remove unnecessary rows
      while (words.rowCount() < table.getRowCount()) {
        table.removeRow(table.getRowCount() - 1);
      }
    }
  }
}
