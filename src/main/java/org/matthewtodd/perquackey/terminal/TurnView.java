package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableHeaderRenderer;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.function.Consumer;
import org.matthewtodd.terminal.View;

class TurnView extends View<TurnView> {
  final Label score = new Label("");
  final Label timer = new Label("");
  final Table<String> words = new Table<String>("")
      .setTableHeaderRenderer(new LabelIsWidthRenderer());
  final Label input = new Label("");
  final Label message = new Label("")
      .setForegroundColor(TextColor.ANSI.RED);

  private Consumer<KeyStroke> keyPressListener = c -> { };

  TurnView() {
    super(new BorderLayout());

    addComponent(BorderLayout.Location.TOP,
        new Panel(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(score, GridLayout.createHorizontallyFilledLayoutData(1))
            .addComponent(timer, GridLayout.createHorizontallyEndAlignedLayoutData(1)));
    addComponent(BorderLayout.Location.CENTER,
        new Panel(new BorderLayout())
            .addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.TOP)
            .addComponent(words)
            .addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.BOTTOM));
    addComponent(BorderLayout.Location.BOTTOM,
        new Panel(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(input, GridLayout.createHorizontallyFilledLayoutData(1))
            .addComponent(message, GridLayout.createHorizontallyEndAlignedLayoutData(1)));
  }

  @Override public TerminalPosition getCursorLocation() {
    int column = input.getText().length();
    int row = getPanel().getSize().getRows() - 1;
    return new TerminalPosition(column, row);
  }

  @Override protected Result handleKeyStroke(KeyStroke key) {
    keyPressListener.accept(key);
    return Result.HANDLED;
  }

  void setKeyPressListener(Consumer<KeyStroke> keyPressListener) {
    this.keyPressListener = (keyPressListener != null) ? keyPressListener : c -> { };
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
