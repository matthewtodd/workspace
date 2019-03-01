package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminalListener;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.TestScheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PerquackeyTest {
  @Test public void hookup() {
    AtomicBoolean closed = new AtomicBoolean(false);

    VirtualTerminal terminal = new DefaultVirtualTerminal(new TerminalSize(50, 10));

    terminal.addVirtualTerminalListener(new VirtualTerminalListener() {
      @Override public void onFlush() {

      }

      @Override public void onBell() {

      }

      @Override public void onClose() {
        closed.set(true);
      }

      @Override public void onResized(Terminal terminal, TerminalSize terminalSize) {

      }
    });

    BehaviorProcessor<Long> ticker = BehaviorProcessor.create();
    TestScheduler scheduler = new TestScheduler();

    Perquackey.newBuilder()
        .ticker(ticker)
        .looper(task -> scheduler.schedulePeriodicallyDirect(task, 0, 1, TimeUnit.MILLISECONDS))
        .terminal(terminal)
        .build()
        .start(() -> {});

    scheduler.triggerActions();

    assertThat(contentsOf(terminal)).containsExactly(
        "0 points                             [paused] 3:00",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":                                                 "
    );

    terminal.addInput(new KeyStroke(' ', false, false));
    scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();

    assertThat(contentsOf(terminal)).containsExactly(
        "0 points                                      3:00",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":                                                 "
    );

    terminal.addInput(new KeyStroke('a', false, false));
    terminal.addInput(new KeyStroke('p', false, false));
    terminal.addInput(new KeyStroke('p', false, false));
    terminal.addInput(new KeyStroke('l', false, false));
    terminal.addInput(new KeyStroke('e', false, false));
    scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();

    assertThat(contentsOf(terminal)).containsExactly(
        "0 points                                      3:00",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":apple                                            "
    );

    terminal.addInput(new KeyStroke(KeyType.Enter));
    scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();

    assertThat(contentsOf(terminal)).containsExactly(
        "200 points                                    3:00",
        "──────────────────────────────────────────────────",
        "3   4    5     6      7       8        9          ",
        "         apple                                    ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "──────────────────────────────────────────────────",
        ":                                                 "
    );

    terminal.addInput(new KeyStroke('Q', false, false));
    scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
    scheduler.triggerActions();

    assertThat(closed).isTrue();
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
