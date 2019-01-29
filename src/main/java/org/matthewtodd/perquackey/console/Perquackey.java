package org.matthewtodd.perquackey.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.AbstractComposite;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.AbstractTextGUI;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Perquackey {
  static Builder newBuilder() {
    return new Builder();
  }

  public static void main(String[] args) throws Exception {
    Terminal terminal = new UnixTerminal();

    Flow.Scheduler mainThread = Flow.newScheduler();
    Publisher<Long> ticker = mainThread.ticking();
    Publisher<KeyStroke> input = mainThread.input(terminal::readInput);

    // Builder
    newBuilder()
        .terminal(terminal)
        .input(input)
        .ticker(ticker);

    TurnWorkflow workflow = new TurnWorkflow(new Timer(10L, ticker));

    ViewFactory viewFactory = new ViewFactory();

    UI ui = new UI(terminal);
    // Yuck, try passing a custom InputStream into the Terminal at test time?
    Flow.of(input).subscribe(ui::handleInput);
    mainThread.afterEach(ui::refresh);

    // Run
    Runnable onComplete = mainThread::shutdown;
    Flow.of(workflow.screen())
        .as(viewFactory::buildView)
        .subscribe(ui::setComponent);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(_ignored -> {});

    mainThread.start();
  }

  static class Builder {
    private Terminal terminal;
    private Publisher<KeyStroke> input;
    private Publisher<Long> ticker;

    public Builder terminal(Terminal terminal) {
      this.terminal = terminal;
      return this;
    }

    public Builder input(Publisher<KeyStroke> input) {
      this.input = input;
      return this;
    }

    public Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }
  }



  static class UI {
    private final Window window;
    private final MultiWindowTextGUI gui;

    public UI(Terminal terminal) {
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

      window = new BasicWindow();
      window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.FULL_SCREEN));

      gui = new MultiWindowTextGUI(screen);
      gui.setTheme(new SimpleTheme(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT));
      gui.addWindow(window);
    }

    void setComponent(Component component) {
      window.setComponent(component);
    }

    void handleInput(KeyStroke key) {
      // cf TextGUI#processInput()
      if (gui.handleInput(key)) { // NB skipping unhandled key strokes, not sure we need that feature
        Field dirty;

        try {
          dirty = AbstractTextGUI.class.getDeclaredField("dirty");
        } catch (NoSuchFieldException e) {
          throw new RuntimeException(e);
        }

        dirty.setAccessible(true);

        try {
          dirty.set(gui, true);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }

    void refresh() {
      if (gui.isPendingUpdate()) {
        try {
          gui.updateScreen();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static class ViewFactory {
    Component buildView(WorkflowScreen<?, ?> screen) {
      switch (screen.key) {
        case PausedScreen.KEY:
          return new TurnView(new PausedCoordinator(screen));
        case SpellingScreen.KEY:
          return new TurnView(new SpellingCoordinator(screen));
        default:
          throw new IllegalStateException();
      }
    }
  }

  // could extract bits of this when we have more than one view...
  public static class TurnView extends AbstractComposite<TurnView> {
    public final Label score;
    public final Label timer;
    public final CommandLine input;
    private final Coordinator<TurnView> coordinator;

    public TurnView(Coordinator<TurnView> coordinator) {
      this.coordinator = coordinator;

      score = new Label("");
      timer = new Label("");
      input = new CommandLine();

      Panel panel = new Panel();
      panel.addComponent(score);
      panel.addComponent(timer);
      panel.addComponent(input);
      setComponent(panel);
    }

    @Override protected ComponentRenderer<TurnView> createDefaultRenderer() {
      return new ComponentRenderer<TurnView>() {
        @Override public TerminalSize getPreferredSize(TurnView component) {
          return component.getComponent().getPreferredSize();
        }

        @Override public void drawComponent(TextGUIGraphics graphics, TurnView component) {
          component.getComponent().draw(graphics);
        }
      };
    }

    @Override public synchronized void onAdded(Container container) {
      super.onAdded(container);
      coordinator.attach(this);
    }

    @Override public synchronized void onRemoved(Container container) {
      super.onRemoved(container);
      coordinator.detach(this);
    }
  }

  public static class CommandLine extends AbstractInteractableComponent<CommandLine> {
    private StringBuilder buffer = new StringBuilder();
    private Listener listener = Listener.NONE;

    public CommandLine() {
      setInputFilter((interactable, keyStroke) -> {
        switch (keyStroke.getKeyType()) {
          case Character:
            return Character.isLowerCase(keyStroke.getCharacter())
                || Character.isSpaceChar(keyStroke.getCharacter());
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

    interface Listener {
      Listener NONE = new Listener() {};

      default void onSpace() {}

      default void onEnter(String command) {}
    }

    public void setListener(Listener listener) {
      this.listener = (listener != null) ? listener : Listener.NONE;
    }

    @Override protected InteractableRenderer<CommandLine> createDefaultRenderer() {
      return new InteractableRenderer<CommandLine>() {
        @Override public TerminalPosition getCursorLocation(CommandLine component) {
          return component.getPosition().withRelativeColumn(buffer.length());
        }

        @Override public TerminalSize getPreferredSize(CommandLine component) {
          return new TerminalSize(buffer.length() + 1, 1);
        }

        @Override public void drawComponent(TextGUIGraphics graphics, CommandLine component) {
          graphics.putString(component.getPosition(), String.format(":%s", buffer));
        }
      };
    }

    @Override protected Result handleKeyStroke(KeyStroke keyStroke) {
      switch (keyStroke.getKeyType()) {
        case Character:
          if (Character.isLowerCase(keyStroke.getCharacter())) {
            buffer.append(keyStroke.getCharacter());
            invalidate();
          } else if (Character.isSpaceChar(keyStroke.getCharacter())) {
            listener.onSpace();
          } else {
            throw new IllegalStateException();
          }
          break;
        case Backspace:
          buffer.setLength(Math.max(0, buffer.length() - 1));
          invalidate();
          break;
        case Enter:
          listener.onEnter(buffer.toString());
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
