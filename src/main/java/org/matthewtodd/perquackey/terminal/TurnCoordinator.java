package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.TurnScreen;
import org.matthewtodd.perquackey.WordList;
import org.matthewtodd.terminal.Coordinator;

class TurnCoordinator implements Coordinator<TurnView> {
  private final TurnScreen screen;

  TurnCoordinator(TurnScreen screen) {
    this.screen = screen;
  }

  @Override public void attach(TurnView view) {
    view.input.setKeyPressListener(keyStroke -> {
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

    // TODO get rid of this setTableModel call.
    // turn.words() should be able to provide column info.
    // (especially once we handle vulnerable turns.)
    view.words.setTableModel(new TableModel<>("3", "4", "5", "6", "7", "8", "9"));

    Flow.of(screen.screenData).subscribe(turn -> {
      // TODO could push formatting up into the view widgets.
      view.score.setText(String.format("%d points", turn.score()));

      view.timer.setText(String.format("%s%d:%02d",
          turn.timer().running() ? "" : "[paused] ",
          turn.timer().remaining() / 60,
          turn.timer().remaining() % 60));

      new TableUpdater(view.words.getTableModel()).update(turn.words());

      view.input.setText(String.format(":%s", turn.input().value()));
      view.message.setText(turn.input().message());
    });

    view.input.takeFocus();
  }

  static class TableUpdater {
    private final TableModel<String> table;

    TableUpdater(TableModel<String> tableModel) {
      table = tableModel;
    }

    // TODO rather than passing this object, can we pass access to the data we need?
    // so, words::rowCount as a Supplier<Integer>, for example.
    // But that's premature because I'm not sure how to think about the column headers being so overloaded.
    void update(WordList words) {
      // TODO handle adding/removing columns first

      // update existing rows
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          table.setCell(columnIndex, rowIndex,
              // HACK HERE
              words.getWord(Integer.parseInt(table.getColumnLabel(columnIndex)), rowIndex));
        }
      }

      // add new rows
      while (table.getRowCount() < words.rowCount()) {
        int rowIndex = table.getRowCount();
        Collection<String> row = new ArrayList<>(table.getColumnCount());
        for (int i = 0; i < table.getColumnCount(); i++) {
          // HACK HERE
          row.add(words.getWord(Integer.parseInt(table.getColumnLabel(i)), rowIndex));
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
