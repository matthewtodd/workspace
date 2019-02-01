package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.AbstractComposite;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.flow.Flow.Scheduler;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnScreen;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Perquackey {
  private final TurnWorkflow workflow;
  private final ViewFactory viewFactory;
  private final UI ui;

  private Perquackey(TurnWorkflow workflow, ViewFactory viewFactory, UI ui) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.ui = ui;
  }

  public static void main(String[] args) throws Exception {
    Terminal terminal = new UnixTerminal();
    Scheduler scheduler = Flow.newScheduler();

    Perquackey.newBuilder()
        .terminal(terminal)
        .ticker(scheduler.ticking())
        .looper(scheduler::loop)
        .build()
        .start(scheduler::shutdown);

    scheduler.start();
  }

  static Builder newBuilder() {
    return new Builder();
  }

  void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .as(viewFactory::buildView)
        .subscribe(ui::setComponent);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(_ignored -> {});
  }

  static class Builder {
    private Terminal terminal;
    private Publisher<Long> ticker;
    private Consumer<Runnable> looper;

    Builder terminal(Terminal terminal) {
      this.terminal = terminal;
      return this;
    }

    Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }

    Builder looper(Consumer<Runnable> looper) {
      this.looper = looper;
      return this;
    }

    Perquackey build() {
      TurnWorkflow workflow = new TurnWorkflow(new Timer(180L, ticker));

      ViewFactory viewFactory = new ViewFactory();

      UI ui = new UI(looper, terminal);

      return new Perquackey(workflow, viewFactory, ui);
    }
  }

  static class ViewFactory {
    Component buildView(WorkflowScreen<?, ?> screen) {
      if (TurnScreen.KEY.equals(screen.key)) {
        return new TurnView(new TurnCoordinator(screen));
      }
      throw new IllegalStateException();
    }
  }

  static class UI {
    private final Consumer<Runnable> looper;
    private final Terminal terminal;
    private final Window window;
    private boolean started = false;

    UI(Consumer<Runnable> looper, Terminal terminal) {
      this.looper = looper;
      this.terminal = terminal;
      window = new BasicWindow();
      window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.FULL_SCREEN));
    }

    void setComponent(Component component) {
      if (!started) {
        start();
        started = true;
      }
      window.setComponent(component);
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

  // could extract bits of this when we have more than one view.
  // the main novelty is the coordinator support.
  static class TurnView extends AbstractComposite<TurnView> {
    final Label score;
    final Label timer;
    final Table<String> words;
    final CommandLine input;
    private final Coordinator<TurnView> coordinator;

    TurnView(Coordinator<TurnView> coordinator) {
      this.coordinator = coordinator;

      score = new Label("");
      timer = new Label("");
      words = new Table<>("");
      input = new CommandLine();

      Panel header = new Panel();
      header.setLayoutManager(new GridLayout(2).setLeftMarginSize(0).setRightMarginSize(0));
      header.addComponent(score, GridLayout.createHorizontallyFilledLayoutData(1));
      header.addComponent(timer, GridLayout.createHorizontallyEndAlignedLayoutData(1));

      Panel main = new Panel();
      main.setLayoutManager(new BorderLayout());
      main.addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.TOP);
      main.addComponent(words);
      main.addComponent(new Separator(Direction.HORIZONTAL), BorderLayout.Location.BOTTOM);

      Panel whole = new Panel();
      whole.setLayoutManager(new BorderLayout());
      whole.addComponent(header, BorderLayout.Location.TOP);
      whole.addComponent(main, BorderLayout.Location.CENTER);
      whole.addComponent(input, BorderLayout.Location.BOTTOM);
      setComponent(whole);
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
      coordinator.detach(this);
      super.onRemoved(container);
    }
  }

  static class CommandLine extends AbstractInteractableComponent<CommandLine> {
    private StringBuilder buffer = new StringBuilder();
    private Listener listener = Listener.NONE;

    CommandLine() {
      setInputFilter((component, keyStroke) -> {
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

    void setListener(Listener listener) {
      this.listener = (listener != null) ? listener : Listener.NONE;
    }

    @Override protected InteractableRenderer<CommandLine> createDefaultRenderer() {
      return new InteractableRenderer<CommandLine>() {
        @Override public TerminalPosition getCursorLocation(CommandLine component) {
          return component.getPosition().withRelativeColumn(buffer.length());
        }

        @Override public TerminalSize getPreferredSize(CommandLine component) {
          return TerminalSize.ONE.withRelativeColumns(buffer.length());
        }

        @Override public void drawComponent(TextGUIGraphics graphics, CommandLine component) {
          // N.B. position is relative to the component's drawable area.
          // TODO should we read the buffer from the component? Not sure if these are reused.
          graphics.putString(TerminalPosition.TOP_LEFT_CORNER, String.format(":%s", buffer));
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

    interface Listener {
      Listener NONE = new Listener() {
        @Override public void onSpace() { }
        @Override public void onEnter(String command) { }
      };

      void onSpace();
      void onEnter(String command);
    }
  }
}
