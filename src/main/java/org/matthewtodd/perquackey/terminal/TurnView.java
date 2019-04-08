package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableHeaderRenderer;
import org.matthewtodd.terminal.CommandLine;
import org.matthewtodd.terminal.View;

class TurnView extends View<TurnView> {
  final Label score = new Label("");
  final Label timer = new Label("");
  final Table<String> words = new Table<String>("0")
      .setTableHeaderRenderer(new LabelIsWidthRenderer());
  final CommandLine commandLine = new CommandLine();
  final Label letters = new Label("");

  // TODO I don't like the mutability here.
  // View just gives us listeners.
  // If we had ids for components and a findById method, we wouldn't need to subclass View.
  TurnView() {
    setComponent(new Panel(new BorderLayout())
        .addComponent(new Panel(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(timer, GridLayout.createHorizontallyFilledLayoutData(1))
            .addComponent(score, GridLayout.createHorizontallyEndAlignedLayoutData(1))
            .setLayoutData(BorderLayout.Location.TOP))
        .addComponent(new Panel(new BorderLayout())
            .addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.TOP)
            .addComponent(words)
            .addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.BOTTOM)
            .setLayoutData(BorderLayout.Location.CENTER))
        .addComponent(new Panel(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(commandLine, GridLayout.createHorizontallyFilledLayoutData(1))
            .addComponent(letters, GridLayout.createHorizontallyEndAlignedLayoutData(1))
            .setLayoutData(BorderLayout.Location.BOTTOM)));
  }

  private static class LabelIsWidthRenderer implements TableHeaderRenderer<String> {
    @Override
    public TerminalSize getPreferredSize(Table<String> table, String label, int columnIndex) {
      // HACK HERE
      return new TerminalSize(Integer.parseInt(label), 1);
    }

    @Override
    public void drawHeader(Table<String> table, String label, int index, TextGUIGraphics graphics) {
      graphics.putString(TerminalPosition.TOP_LEFT_CORNER, label);
    }
  }
}
