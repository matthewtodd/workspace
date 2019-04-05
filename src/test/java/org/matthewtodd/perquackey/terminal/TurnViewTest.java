package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.ObjectArrayAssert;
import org.junit.Test;
import org.matthewtodd.terminal.TerminalUI;
import org.matthewtodd.terminal.View;

import static org.assertj.core.api.Assertions.assertThat;

public class TurnViewTest {
  @Test public void hookup() {
    ViewTester<TurnView> tester = new ViewTester<>(new TurnView());

    tester.update(view -> {
      view.score.setText("1900 points");
      view.timer.setText("1:42");
      view.words.setTableModel(new TableModel<>("3", "4", "5", "6", "7", "8", "9"));
      view.commandLine.setText(":za");
      view.letters.setText("abc");
    });

    tester.assertRows().containsExactly(
        "1:42                                   1900 points",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":za                                            abc"
    );

    tester.update(view -> view.commandLine.takeFocus());
    tester.assertCursorBufferPosition().isEqualTo(new TerminalPosition(3, 9));
    tester.assertCursorVisible().isTrue();
  }

  static class ViewTester<T extends View<T>> {
    private final T view;

    private VirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(50, 10));
    private AtomicReference<Runnable> looper = new AtomicReference<>();

    ViewTester(T view) {
      this.view = view;
      new TerminalUI(terminal, looper::set).accept(view);
      update(v -> {});
    }

    void update(Consumer<T> changes) {
      changes.accept(view);
      looper.get().run();
    }

    ObjectArrayAssert<String> assertRows() {
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

    AbstractComparableAssert<?, TerminalPosition> assertCursorBufferPosition() {
      return assertThat(terminal.getCursorBufferPosition());
    }

    AbstractBooleanAssert<?> assertCursorVisible() {
      return assertThat(terminal.isCursorVisible());
    }
  }
}
