package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import io.reactivex.processors.BehaviorProcessor;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.matthewtodd.terminal.Application;
import org.matthewtodd.terminal.View;

import static org.assertj.core.api.Assertions.assertThat;

// This test shows the UI wired up to the application.
// Just a few simple connections will suffice.
// More logic tests should go around the workflow.
// The view tests show what's on the screen.
public class PerquackeyTest {
  private PerquackeyTester perquackey;

  @Before public void setUp() {
    perquackey = new PerquackeyTester();
    perquackey.start();
  }

  @Test public void words() {
    perquackey.on(TurnView.class, view -> {
      assertThat(view.words.getTableModel().getRowCount()).isEqualTo(0);
      perquackey.type("apple").typeEnter();
      assertThat(view.words.getTableModel().getCell(2, 0)).isEqualTo("apple");
    });
  }

  @Test public void letters() {
    perquackey.on(TurnView.class, view -> {
      assertThat(view.letters.getText()).isEmpty();
      perquackey.type("apple").typeEnter();
      assertThat(view.letters.getText()).isEqualTo("aelpp");
    });
  }

  @Test public void columnHeaders() {
    perquackey.on(TurnView.class, view -> assertThat(view.words.getTableModel().getColumnLabels())
        .containsExactly("3", "4", "5", "6", "7", "8", "9"));
  }

  @Test public void input() {
    perquackey.on(TurnView.class, view -> {
      assertThat(view.commandLine.getText()).isEqualTo(":");
      perquackey.type("apple");
      assertThat(view.commandLine.getText()).isEqualTo(":apple");
      perquackey.typeEnter();
      assertThat(view.commandLine.getText()).isEqualTo(":");
    });
  }

  @Test public void inputRejected() {
    perquackey.on(TurnView.class, view -> {
      perquackey.type("za").typeEnter();
      assertThat(view.words.getTableModel().getRowCount()).isEqualTo(0);
    });
  }

  @Test public void inputRejectedRemains() {
    perquackey.on(TurnView.class, view -> {
      perquackey.type("za").typeEnter();
      assertThat(view.commandLine.getText()).isEqualTo(":za");
    });
  }

  @Test public void scoring() {
    perquackey.on(TurnView.class, view -> {
      assertThat(view.score.getText()).isEqualTo("0 points");
      perquackey.type("apple").typeEnter();
      assertThat(view.score.getText()).isEqualTo("200 points");
    });
  }

  @Test public void timer() {
    perquackey.on(TurnView.class, view -> {
      assertThat(view.timer.getText()).isEqualTo("3:00 [paused]");
      perquackey.timerTicks();
      assertThat(view.timer.getText()).isEqualTo("3:00 [paused]");
      perquackey.type(' ');
      assertThat(view.timer.getText()).isEqualTo("3:00");
      perquackey.timerTicks();
      assertThat(view.timer.getText()).isEqualTo("2:59");
    });
  }

  @Test public void quitting() {
    assertThat(perquackey.closed()).isFalse();
    perquackey.on(TurnView.class, view -> perquackey.type('Q'));
    assertThat(perquackey.closed()).isTrue();
  }

  private static class PerquackeyTester {
    private final BehaviorProcessor<Long> ticker;
    private final NullTerminal terminal;
    private final AtomicReference<Runnable> looper;
    private final Application application;

    PerquackeyTester() {
      ticker = BehaviorProcessor.create();
      terminal = new NullTerminal();
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

    PerquackeyTester type(String input) {
      input.chars().forEachOrdered(c -> type((char) c));
      return this;
    }

    void typeEnter() {
      type(new KeyStroke(KeyType.Enter));
    }

    private void type(KeyStroke keyStroke) {
      terminal.addInput(keyStroke);
      looper.get().run();
    }

    void timerTicks() {
      ticker.onNext(0L);
    }

    boolean closed() {
      return terminal.closed();
    }
  }

  // Just a little lighter than a real terminal. Seems to help with test time!
  private static class NullTerminal implements Terminal {
    private final BlockingQueue<KeyStroke> input;
    private final AtomicBoolean closed;

    NullTerminal() {
      this.input = new LinkedBlockingQueue<>();
      this.closed = new AtomicBoolean(false);
    }

    void addInput(KeyStroke keyStroke) {
      input.add(keyStroke);
    }

    boolean closed() {
      return closed.get();
    }

    @Override public void enterPrivateMode() throws IOException {

    }

    @Override public void exitPrivateMode() throws IOException {

    }

    @Override public void clearScreen() throws IOException {

    }

    @Override public void setCursorPosition(int i, int i1) throws IOException {

    }

    @Override public void setCursorPosition(TerminalPosition terminalPosition)
        throws IOException {

    }

    @Override public TerminalPosition getCursorPosition() throws IOException {
      return null;
    }

    @Override public void setCursorVisible(boolean b) throws IOException {

    }

    @Override public void putCharacter(char c) throws IOException {

    }

    @Override public TextGraphics newTextGraphics() throws IOException {
      return null;
    }

    @Override public void enableSGR(SGR sgr) throws IOException {

    }

    @Override public void disableSGR(SGR sgr) throws IOException {

    }

    @Override public void resetColorAndSGR() throws IOException {

    }

    @Override public void setForegroundColor(TextColor textColor) throws IOException {

    }

    @Override public void setBackgroundColor(TextColor textColor) throws IOException {

    }

    @Override public void addResizeListener(TerminalResizeListener terminalResizeListener) {

    }

    @Override public void removeResizeListener(TerminalResizeListener terminalResizeListener) {

    }

    @Override public TerminalSize getTerminalSize() throws IOException {
      return TerminalSize.ONE;
    }

    @Override public byte[] enquireTerminal(int i, TimeUnit timeUnit) throws IOException {
      return new byte[0];
    }

    @Override public void bell() throws IOException {

    }

    @Override public void flush() throws IOException {

    }

    @Override public void close() throws IOException {
      closed.set(true);
    }

    @Override public KeyStroke pollInput() throws IOException {
      return input.poll();
    }

    @Override public KeyStroke readInput() throws IOException {
      try {
        return input.take();
      } catch (InterruptedException e) {
        throw new IOException(e);
      }
    }
  }
}
