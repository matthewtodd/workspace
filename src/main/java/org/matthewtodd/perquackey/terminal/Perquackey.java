package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.IOException;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.flow.Flow.Scheduler;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnScreen;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.terminal.Application;
import org.matthewtodd.terminal.TerminalUI;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Perquackey {
  public static void main(String[] args) throws IOException {
    Scheduler scheduler = Flow.newScheduler();

    Perquackey.newBuilder()
        .ticker(scheduler.ticking())
        .terminal(new UnixTerminal())
        .looper(scheduler::loop)
        .build()
        .start(scheduler::shutdown);

    scheduler.start();
  }

  static Builder newBuilder() {
    return new Builder();
  }

  private static Component viewFactory(WorkflowScreen<?, ?> screen) {
    if (TurnScreen.KEY.equals(screen.key)) {
      return new TurnView(new TurnCoordinator((TurnScreen) screen));
    }
    throw new IllegalStateException();
  }

  static class Builder {
    private Publisher<Long> ticker;
    private Terminal terminal;
    private Consumer<Runnable> looper;

    Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }

    Builder terminal(Terminal terminal) {
      this.terminal = terminal;
      return this;
    }

    Builder looper(Consumer<Runnable> looper) {
      this.looper = looper;
      return this;
    }

    Application build() {
      return new Application(
          new TurnWorkflow(new Timer(180L, ticker)),
          Perquackey::viewFactory,
          new TerminalUI(terminal, looper));
    }
  }
}
