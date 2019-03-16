package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.InteractableRenderer;
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
  final CommandLine commandLine = new CommandLine();
  final Label letters = new Label("");

  // TODO I don't like the mutability here.
  // View just gives us listeners.
  // If we had ids for components and a findById method, we wouldn't need to subclass View.
  TurnView() {
    setComponent(new Panel(new BorderLayout())
        .addComponent(new Panel(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(score, GridLayout.createHorizontallyFilledLayoutData(1))
            .addComponent(timer, GridLayout.createHorizontallyEndAlignedLayoutData(1))
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

  static class CommandLine extends AbstractInteractableComponent<CommandLine> {
    private Consumer<KeyStroke> keyPressListener = c -> { };
    private String text = "";

    String getText() {
      return text;
    }

    void setText(String text) {
      this.text = text;
    }

    @Override protected Result handleKeyStroke(KeyStroke key) {
      keyPressListener.accept(key);
      return Result.HANDLED;
    }

    void setKeyPressListener(Consumer<KeyStroke> keyPressListener) {
      this.keyPressListener = (keyPressListener != null) ? keyPressListener : c -> { };
    }

    @Override protected InteractableRenderer<CommandLine> createDefaultRenderer() {
      return new InteractableRenderer<CommandLine>() {
        @Override public TerminalPosition getCursorLocation(CommandLine commandLine) {
          return TerminalPosition.TOP_LEFT_CORNER.withRelativeColumn(commandLine.getText().length());
        }

        @Override public TerminalSize getPreferredSize(CommandLine commandLine) {
          return TerminalSize.ONE.withRelativeColumns(commandLine.getText().length());
        }

        @Override
        public void drawComponent(TextGUIGraphics textGUIGraphics, CommandLine commandLine) {
          textGUIGraphics.putString(TerminalPosition.TOP_LEFT_CORNER, commandLine.getText());
        }
      };
    }
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
