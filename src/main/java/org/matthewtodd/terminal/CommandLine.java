package org.matthewtodd.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.function.Consumer;

public class CommandLine extends AbstractInteractableComponent<CommandLine> {
  private Consumer<KeyStroke> keyPressListener = c -> { };
  private String text = ":";

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = String.format(":%s", text);
  }

  @Override protected Result handleKeyStroke(KeyStroke key) {
    keyPressListener.accept(key);
    return Result.HANDLED;
  }

  public void setKeyPressListener(Consumer<KeyStroke> keyPressListener) {
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
