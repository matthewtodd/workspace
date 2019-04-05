package org.matthewtodd.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.ObjectArrayAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class ViewTester<T extends View<T>> {
  private final T view;

  private VirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(50, 10));
  private AtomicReference<Runnable> looper = new AtomicReference<>();

  public ViewTester(T view) {
    this.view = view;
    new TerminalUI(terminal, looper::set).accept(view);
    update(v -> {});
  }

  public void update(Consumer<T> changes) {
    changes.accept(view);
    looper.get().run();
  }

  public ObjectArrayAssert<String> assertRows() {
    StringBuilder contents = new StringBuilder();

    int numberOfRows = terminal.getTerminalSize().getRows();
    int numberOfColumns = terminal.getTerminalSize().getColumns();

    terminal.forEachLine(0, numberOfRows - 1, (rowNumber, row) -> {
      for (int columnNumber = 0; columnNumber < numberOfColumns; columnNumber++) {
        contents.append(row.getCharacterAt(columnNumber).getCharacter());
      }
      contents.append('\n');
    });

    return assertThat(contents.toString().split("\n"));
  }

  public AbstractComparableAssert<?, TerminalPosition> assertCursorBufferPosition() {
    return assertThat(terminal.getCursorBufferPosition());
  }

  public AbstractBooleanAssert<?> assertCursorVisible() {
    return assertThat(terminal.isCursorVisible());
  }
}
