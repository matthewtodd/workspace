package org.matthewtodd.terminal;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public class TerminalUI implements Consumer<Component> {
  private final Consumer<Runnable> looper;
  private final Terminal terminal;
  private final Window window;
  private boolean started = false;

  public TerminalUI(Terminal terminal, Consumer<Runnable> looper) {
    this.looper = looper;
    this.terminal = terminal;
    window = new BasicWindow();
    window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.FULL_SCREEN));
  }

  // TODO maybe don't auto-start?
  // the looper isn't a dependency, just a collaborator.
  @Override public void accept(Component component) {
    if (!started) {
      start();
      started = true;
    }
    window.setComponent(component);
  }

  public void close() {
    try {
      window.getTextGUI().getScreen().stopScreen();
      terminal.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void start() {
    System.setProperty("java.awt.headless", "true");
    Screen screen;

    try {
      screen = new TerminalScreen(terminal);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      screen.startScreen();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
    gui.setTheme(new SimpleTheme(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT));
    gui.addWindow(window);

    // TODO maybe there's a better way to hook this into the scheduler.
    // Really we just want it to happen after anything *else* happens.
    looper.accept(() -> {
      try {
        gui.getGUIThread().processEventsAndUpdate();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
