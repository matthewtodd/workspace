package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminal;
import com.googlecode.lanterna.terminal.virtual.VirtualTerminalListener;
import io.reactivex.processors.BehaviorProcessor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.Test;
import org.matthewtodd.terminal.Application;
import org.matthewtodd.terminal.View;

import static org.assertj.core.api.Assertions.assertThat;

public class PerquackeyTest {
  @Test public void hookup() {
    PerquackeyTester perquackey = new PerquackeyTester();
    perquackey.start();

    perquackey.on(TurnView.class, view -> {
      assertThat(view.score.getText()).isEqualTo("0 points");
      assertThat(view.timer.getText()).isEqualTo("[paused] 3:00");
      assertThat(view.words.getTableModel().getColumnLabels())
          .containsExactly("3", "4", "5", "6", "7", "8", "9");
      assertThat(view.words.getTableModel().getRowCount()).isEqualTo(0);
      assertThat(view.input.getText()).isEqualTo("");

      perquackey.type(' ');
      assertThat(view.timer.getText()).isEqualTo("3:00");

      perquackey.type("apple");
      assertThat(view.score.getText()).isEqualTo("0 points");
      assertThat(view.input.getText()).isEqualTo("apple");

      perquackey.typeEnter();
      assertThat(view.score.getText()).isEqualTo("200 points");
      assertThat(view.words.getTableModel().getCell(2, 0)).isEqualTo("apple");
      assertThat(view.input.getText()).isEqualTo("");

      perquackey.type('Q');
    });

    assertThat(perquackey.closed()).isTrue();
  }

  private static class PerquackeyTester {
    private final BehaviorProcessor<Long> ticker;
    private final VirtualTerminal terminal;
    private final AtomicReference<Runnable> looper;
    private final Application application;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    PerquackeyTester() {
      ticker = BehaviorProcessor.create();
      terminal = new DefaultVirtualTerminal();
      terminal.addVirtualTerminalListener(new TerminalListener());
      looper = new AtomicReference<>();
      application = Perquackey.newBuilder()
          .ticker(ticker)
          .terminal(terminal)
          .looper(looper::set)
          .build();
    }

    void start() {
      application.start(() -> {});
    }

    <T extends View> void on(Class<T> viewClass, Consumer<T> assertions) {
      assertions.accept(application.currentView(viewClass));
    }

    void type(char c) {
      type(new KeyStroke(c, false, false));
    }

    void type(String input) {
      input.chars().forEachOrdered(c -> type((char) c));
    }

    void typeEnter() {
      type(new KeyStroke(KeyType.Enter));
    }

    private void type(KeyStroke keyStroke) {
      terminal.addInput(keyStroke);
      looper.get().run();
    }

    boolean closed() {
      return closed.get();
    }

    class TerminalListener implements VirtualTerminalListener {
      @Override public void onFlush() {

      }

      @Override public void onBell() {

      }

      @Override public void onClose() {
        closed.set(true);
      }

      @Override public void onResized(Terminal terminal, TerminalSize newSize) {

      }
    }
  }
}
