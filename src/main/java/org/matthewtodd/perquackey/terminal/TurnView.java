package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComposite;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.util.function.Consumer;
import org.matthewtodd.terminal.Coordinator;
import org.matthewtodd.terminal.CoordinatorView;

class TurnView extends CoordinatorView<TurnView> {
  final Label score;
  final Label timer;
  final Table<String> words;
  final CommandLine input;
  private Consumer<Character> keyPressListener = c -> {};

  TurnView(Coordinator<TurnView> coordinator) {
    super(coordinator);

    score = new Label("");
    timer = new Label("");
    words = new Table<>("");
    input = new CommandLine();

    Panel header = new Panel();
    header.setLayoutManager(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0));
    header.addComponent(score, GridLayout.createHorizontallyFilledLayoutData(1));
    header.addComponent(timer, GridLayout.createHorizontallyEndAlignedLayoutData(1));

    Panel main = new Panel();
    main.setLayoutManager(new BorderLayout());
    main.addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.TOP);
    main.addComponent(words);
    main.addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.BOTTOM);

    Panel whole = new Panel();
    whole.setLayoutManager(new BorderLayout());
    whole.addComponent(header, BorderLayout.Location.TOP);
    whole.addComponent(main, BorderLayout.Location.CENTER);
    whole.addComponent(input, BorderLayout.Location.BOTTOM);
    setComponent(whole);
  }

  @Override public boolean handleInput(KeyStroke key) {
    if (key.getKeyType().equals(KeyType.Character)) {
      keyPressListener.accept(key.getCharacter());
      return true;
    } else {
      return super.handleInput(key);
    }
  }

  void setKeyPressListener(Consumer<Character> keyPressListener) {
    this.keyPressListener = (keyPressListener != null) ? keyPressListener : c -> {};
  }

  static class CommandLine extends AbstractInteractableComponent<CommandLine> {
    private StringBuilder buffer = new StringBuilder();
    private Consumer<String> listener = word -> {};

    CommandLine() {
      setInputFilter((component, keyStroke) -> {
        switch (keyStroke.getKeyType()) {
          case Character:
            return Character.isLowerCase(keyStroke.getCharacter());
          case Backspace:
            return true;
          case Enter:
            return true;
          default:
            return false;
        }
      });
      invalidate();
    }

    void setListener(Consumer<String> listener) {
      this.listener = (listener != null) ? listener : word -> {};
    }

    @Override protected InteractableRenderer<CommandLine> createDefaultRenderer() {
      return new InteractableRenderer<CommandLine>() {
        @Override public TerminalPosition getCursorLocation(CommandLine component) {
          return component.getPosition().withRelativeColumn(buffer.length());
        }

        @Override public TerminalSize getPreferredSize(CommandLine component) {
          return TerminalSize.ONE.withRelativeColumns(buffer.length());
        }

        @Override public void drawComponent(TextGUIGraphics graphics, CommandLine component) {
          // N.B. position is relative to the component's drawable area.
          // TODO should we read the buffer from the component? Not sure if these are reused.
          graphics.putString(TerminalPosition.TOP_LEFT_CORNER, String.format(":%s", buffer));
        }
      };
    }

    @Override protected Result handleKeyStroke(KeyStroke keyStroke) {
      switch (keyStroke.getKeyType()) {
        case Character:
          if (Character.isLowerCase(keyStroke.getCharacter())) {
            buffer.append(keyStroke.getCharacter());
            invalidate();
          } else {
            throw new IllegalStateException();
          }
          break;
        case Backspace:
          buffer.setLength(Math.max(0, buffer.length() - 1));
          invalidate();
          break;
        case Enter:
          listener.accept(buffer.toString());
          buffer.setLength(0);
          invalidate();
          break;
        default:
          throw new IllegalStateException();
      }

      return Result.HANDLED;
    }
  }
}
