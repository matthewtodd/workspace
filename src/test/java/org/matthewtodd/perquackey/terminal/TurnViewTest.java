package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.matthewtodd.terminal.TerminalUI;

import static org.assertj.core.api.Assertions.assertThat;

public class TurnViewTest {
  @Test public void hookup() {
    TurnView view = new TurnView();

    VirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(50, 10));
    AtomicReference<Runnable> looper = new AtomicReference<>();
    // TODO maybe it's wonky to reuse our (ill-named) TerminalUI here.
    // Maybe something Lanterna-native instead?
    TerminalUI ui = new TerminalUI(terminal, looper::set);
    ui.accept(view);

    view.score.setText("1900 points");
    view.timer.setText("1:42");
    view.words.setTableModel(new TableModel<>("3", "4", "5", "6", "7", "8", "9"));
    view.commandLine.setText(":za");
    view.commandLine.takeFocus();

    looper.get().run();

    assertThat(contentsOf(terminal)).containsExactly(
        "1900 points                                   1:42",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":za                                               "
    );

    assertThat(terminal.getCursorBufferPosition().getColumn()).isEqualTo(3);
    assertThat(terminal.getCursorBufferPosition().getRow()).isEqualTo(9);
    assertThat(terminal.isCursorVisible()).isTrue();
  }

  private Collection<String> contentsOf(VirtualTerminal terminal) {
    Collection<String> contents = new ArrayList<>(terminal.getTerminalSize().getRows());
    for (int r = 0; r < terminal.getTerminalSize().getRows(); r++) {
      StringBuilder row = new StringBuilder();
      for (int c = 0; c < terminal.getTerminalSize().getColumns(); c++) {
        row.append(terminal.getCharacter(c, r).getCharacter());
      }
      contents.add(row.toString());
    }
    return contents;
  }
}
