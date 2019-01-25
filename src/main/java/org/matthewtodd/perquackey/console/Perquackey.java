package org.matthewtodd.perquackey.console;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.AbstractTextGUI;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Perquackey {
  static Builder newBuilder() {
    return new Builder();
  }

  // Interesting, this smells like a Guice module.
  static class Builder {
    private Flow.Scheduler scheduler;
    private Terminal terminal;
    private Publisher<Long> ticker;

    Builder scheduler(Flow.Scheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    Builder terminal(Terminal terminal) {
      this.terminal = terminal;
      return this;
    }

    Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }

    Application build() {
      return new Application(workflow(), viewFactory(), terminal, scheduler);
    }

    private Workflow<?, ?> workflow() {
      return new TurnWorkflow(new Timer(180L, ticker));
    }

    private Function<WorkflowScreen<?, ?>, Component> viewFactory() {
      return (_ignored) -> new Label("Yay?");
    }
  }

  //public static void main(String[] args) throws IOException {
  //  Flow.Scheduler mainThread = Flow.newScheduler();
  //  Terminal terminal = new UnixTerminal();
  //
  //  Perquackey.newBuilder()
  //      .ticker(mainThread.ticking())
  //      .scheduler(mainThread)
  //      .terminal(terminal)
  //      .build()
  //      .run(mainThread);
  //}

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");

    UnixTerminal terminal = new UnixTerminal();
    TerminalScreen screen = new TerminalScreen(terminal);
    screen.startScreen();

    Window window = new BasicWindow();
    window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.FULL_SCREEN));

    WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
    gui.setTheme(new SimpleTheme(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT));
    gui.addWindow(window);

    BlockingScheduler scheduler = new BlockingScheduler();
    TurnWorkflow workflow = new TurnWorkflow(
        new Timer(180L, Flowable.interval(1, TimeUnit.SECONDS).observeOn(scheduler)));

    Flow.of(workflow.screen())
        .as(s -> new Label(s.key)) // <-- becomes view factory
        .subscribe(window::setComponent);

    Flow.of(workflow.result())
        .onComplete(scheduler::shutdown)
        .subscribe(_ignored -> {});

    // TODO deal with this disposable
    Disposable io = Flowable.<KeyStroke>generate(emitter -> emitter.onNext(screen.readInput()))
        .subscribeOn(Schedulers.io())
        .observeOn(scheduler)
        .subscribe(key -> {
          // cf TextGUI#processInput()
          if (window.handleInput(key)) { // NB skipping unhandled key strokes, not sure we need that feature
            Field dirty = AbstractTextGUI.class.getDeclaredField("dirty");
            dirty.setAccessible(true);
            dirty.set(gui, true);
          }
        });

    // TODO make this more reactive?
    // really what we want is to call this after anything happens in the scheduler?
    // so, really, we'd just be wrapping every task submitted.
    scheduler.schedulePeriodicallyDirect(() -> {
      if (gui.isPendingUpdate()) {
        try {
          gui.updateScreen();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, 0, 1, TimeUnit.MILLISECONDS);

    scheduler.execute();
  }
}
