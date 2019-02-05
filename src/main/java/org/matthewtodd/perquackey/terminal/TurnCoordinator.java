package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableHeaderRenderer;
import com.googlecode.lanterna.gui2.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.TurnScreen;
import org.matthewtodd.workflow.WorkflowScreen;

class TurnCoordinator implements Coordinator<Perquackey.TurnView>, Perquackey.CommandLine.Listener,
    TableHeaderRenderer<String> {
   private final TurnScreen screen;

   TurnCoordinator(WorkflowScreen<?, ?> screen) {
     this.screen = (TurnScreen) screen;
   }

  @Override public void attach(Perquackey.TurnView view) {
    view.input.setListener(this);
    view.words.setTableModel(new TableModel<>("3", "4", "5", "6", "7", "8", "9"));
    view.words.setTableHeaderRenderer(this);

    Flow.of(screen.screenData).subscribe(turn -> {
      view.score.setText(String.format("%d points", turn.score()));

      view.timer.setText(String.format("%s%d:%02d",
          turn.timer().running() ? "" : "[paused] ",
          turn.timer().remaining() / 60,
          turn.timer().remaining() % 60));

      TableModel<String> table = view.words.getTableModel();

      // update existing rows
      for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
          table.setCell(columnIndex, rowIndex,
              turn.words().getWord(Integer.parseInt(table.getColumnLabel(columnIndex)), rowIndex));
        }
      }

      // add new rows
      while (table.getRowCount() < turn.words().rowCount()) {
        int rowIndex = table.getRowCount();
        Collection<String> row = new ArrayList<>(table.getColumnCount());
        for (int i = 0; i < table.getColumnCount(); i++) {
          row.add(turn.words().getWord(Integer.parseInt(table.getColumnLabel(i)), rowIndex));
        }
        table.addRow(row);
      }

      // remove unnecessary rows
      while (turn.words().rowCount() < table.getRowCount()) {
        table.removeRow(table.getRowCount() - 1);
      }
    });

    view.input.takeFocus();
  }

  @Override public void detach(Perquackey.TurnView view) {
    view.input.setListener(null);
  }

  @Override public void onSpace() {
    screen.eventHandler.toggleTimer();
  }

  @Override public void onEnter(String word) {
    screen.eventHandler.spell(word);
  }

  @Override public void onQuit() {
    screen.eventHandler.quit();
  }

  @Override
  public TerminalSize getPreferredSize(Table<String> table, String label, int columnIndex) {
    return new TerminalSize(Integer.parseInt(label), 1);
  }

  @Override
  public void drawHeader(Table<String> table, String label, int index, TextGUIGraphics graphics) {
    graphics.putString(TerminalPosition.TOP_LEFT_CORNER, label);
  }
}
