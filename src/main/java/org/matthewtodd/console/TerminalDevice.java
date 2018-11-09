package org.matthewtodd.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class TerminalDevice implements Device {
  private final InputStream input;
  private final PrintStream output;
  private final Size size;

  public static TerminalDevice instance() throws IOException {
    Terminal terminal = TerminalBuilder.terminal();
    terminal.enterRawMode();
    return new TerminalDevice(terminal);
  }

  private TerminalDevice(Terminal terminal) {
    input = terminal.input();
    output = new PrintStream(terminal.output());
    size = terminal.getSize();
  }

  // "\u001B[?25l" hides the cursor
  // "\u001B[?25h" shows the cursor
  @Override public int rows() {
    return size.getRows();
  }

  @Override public int columns() {
    return size.getColumns();
  }

  @Override public void replace(int row, int column, String content) {
    output.printf("\u001B[%d;%dH", row, column); // Move cursor to row,column.
    output.print(content);
    output.flush();
  }

  @Override public void clear() {
    output.print("\u001B[2J"); // Clear screen.
    output.print("\u001B[3J"); // Clear scrollback buffer, too.
    output.print("\u001B[H");  // Move cursor to top-left corner.
    output.flush();
  }

  public InputStream input() {
    return input;
  }
}
