package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import java.io.IOException;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.flow.Flow.Scheduler;
import org.matthewtodd.perquackey.Announcement;
import org.matthewtodd.perquackey.GameWorkflow;
import org.matthewtodd.perquackey.ScorecardScreen;
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
        .announcer(new SayAnnouncer())
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
    switch (screen.key) {
      case TurnScreen.KEY:
        TurnCoordinator turnCoordinator = new TurnCoordinator((TurnScreen) screen);
        TurnView turnView = new TurnView();
        turnView.setAddedListener(turnCoordinator::attach);
        turnView.setRemovedListener(turnCoordinator::detach);
        return turnView;
      case ScorecardScreen.KEY:
        ScorecardCoordinator scorecardCoordinator = new ScorecardCoordinator((ScorecardScreen) screen);
        ScorecardView scorecardView = new ScorecardView();
        scorecardView.setAddedListener(scorecardCoordinator::attach);
        scorecardView.setRemovedListener(scorecardCoordinator::detach);
        return scorecardView;
      default:
        throw new IllegalStateException(String.format("View factory can't handle %s.", screen.key));
    }
  }

  static class Builder {
    private Publisher<Long> ticker;
    private Consumer<Announcement> announcer;
    private Terminal terminal;
    private Consumer<Runnable> looper;

    Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }

    Builder announcer(Consumer<Announcement> announcer) {
      this.announcer = announcer;
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
          new GameWorkflow(() -> new TurnWorkflow(ticker, announcer)),
          Perquackey::viewFactory,
          new TerminalUI(terminal, looper));
    }
  }

  static class SayAnnouncer implements Consumer<Announcement> {
    @Override public void accept(Announcement announcement) {
      say(messageFor(announcement));
    }

    private void say(String message) {
      try {
        new ProcessBuilder("say", "-v", "Alex", "-r", "260", message).start();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private String messageFor(Announcement announcement) {
      if (announcement == Announcement.TimeIsUp) {
        return "Time's up!";
      } else {
        throw new IllegalStateException("Unexpected announcement.");
      }
    }
  }
}
