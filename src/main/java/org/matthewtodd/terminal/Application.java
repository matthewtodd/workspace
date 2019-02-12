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
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;

public class Application {
  private final Workflow<?, ?> workflow;
  private final Function<WorkflowScreen<?, ?>, Component> viewFactory;
  private final UI ui;

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Workflow<?, ?> workflow;
    private Function<WorkflowScreen<?, ?>, Component> viewFactory;
    private Consumer<Runnable> looper;
    private Terminal terminal;

    private Builder() {
      // Default to the Terminal we'd always use in production.
      try {
        this.terminal = new UnixTerminal();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Builder workflow(Workflow<?, ?> workflow) {
      this.workflow = workflow;
      return this;
    }

    public Builder viewFactory(Function<WorkflowScreen<?, ?>, Component> viewFactory) {
      this.viewFactory = viewFactory;
      return this;
    }

    public Builder looper(Consumer<Runnable> looper) {
      this.looper = looper;
      return this;
    }

    public Builder terminal(Terminal terminal) {
      this.terminal = terminal;
      return this;
    }

    public Application build() {
      return new Application(workflow, viewFactory, new UI(looper, terminal));
    }
  }

  private Application(Workflow<?, ?> workflow, Function<WorkflowScreen<?, ?>, Component> viewFactory, UI ui) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.ui = ui;
  }

  public void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .as(viewFactory::apply)
        .subscribe(ui::setComponent);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(_ignored -> ui.close());
  }

  private static class UI {
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

    void close() {
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
}
