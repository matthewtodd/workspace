package org.matthewtodd.perquackey.console;

import java.io.IOException;
import java.util.Set;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.matthewtodd.console.Canvas;
import org.matthewtodd.console.Coordinator;
import org.matthewtodd.console.View;
import org.matthewtodd.console.Window;
import org.matthewtodd.console.Window.KeyPress;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Console {
  private Runnable onComplete;
  private final Window window;
  private final TurnWorkflow workflow;

  Console(Window window, Publisher<Long> ticker) {
    this.onComplete = () -> {};
    this.window = window;
    this.workflow = new TurnWorkflow(new Timer(180L, ticker));
  }

  void start() {
    Flow.of(workflow.screen()).onComplete(onComplete).subscribe(screen -> {
      window.rootView(buildViewForScreen(screen));
    });
    workflow.start(null);
  }

  private void doOnComplete(Runnable runnable) {
    this.onComplete = runnable;
  }

  private View buildViewForScreen(WorkflowScreen<?, ?> screen) {
    // this maybe becomes a loading cache?
    switch (screen.key) {
      case PausedScreen.KEY:
        PausedCoordinator pausedCoordinator = new PausedCoordinator((PausedScreen) screen);
        return new PausedView()
            .setAttachmentListener(pausedCoordinator::attach)
            .setDetachmentListener(pausedCoordinator::detach);
      case SpellingScreen.KEY:
        SpellingCoordinator spellingCoordinator = new SpellingCoordinator((SpellingScreen) screen);
        return new SpellingView()
            .setAttachmentListener(spellingCoordinator::attach)
            .setDetachmentListener(spellingCoordinator::detach);
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  private static class PausedView extends View<PausedView> {
    @Override protected void onDraw(Canvas canvas) {

    }

    @Override protected void onLayout(int left, int top, int right, int bottom) {

    }

    void timer(Timer.Snapshot timer) {
      // HACK so I can see something.
      System.out.println(timer);
    }
  }

  private static class SpellingView extends View<SpellingView> {
    @Override protected void onDraw(Canvas canvas) {

    }

    @Override protected void onLayout(int left, int top, int right, int bottom) {

    }

    void timer(Timer.Snapshot timer) {
      // HACK so I can see something.
      System.out.println(timer);
    }

    void words(Set<String> words) {
      // HACK so I can see something.
      System.out.println(words);
    }
  }

  private static class PausedCoordinator implements Coordinator<PausedView> {
    private final PausedScreen screen;

    PausedCoordinator(PausedScreen screen) {
      this.screen = screen;
    }

    @Override public void attach(PausedView view) {
      Flow.of(screen.screenData).subscribe(turn -> {
        view.timer(turn.timer());
      });

      view.setKeyPressListener(keyPress -> {
        if (keyPress.isSpaceBar()) {
          screen.eventHandler.resumeTimer();
        }
      });
    }
  }

  private static class SpellingCoordinator implements Coordinator<SpellingView> {
    private final SpellingScreen screen;
    private final StringBuilder buffer;

    SpellingCoordinator(SpellingScreen screen) {
      this.screen = screen;
      this.buffer = new StringBuilder();
    }

    @Override public void attach(SpellingView view) {
      Flow.of(screen.screenData).subscribe(turn -> {
        view.timer(turn.timer());
        view.words(turn.words());
      });

      view.setKeyPressListener(keyPress -> {
        if (keyPress.isSpaceBar()) {
          screen.eventHandler.pauseTimer();
        } else if (keyPress.isLowerCaseLetter()) {
          buffer.append(keyPress.stringValue());
          // TODO call view.textView(buffer) or something here
        } else if (keyPress.isEnter()) {
          screen.eventHandler.spell(buffer.toString());
          buffer.setLength(0);
        }
      });
    }
  }

  public static void main(String[] args) throws IOException {
    Flow.Scheduler mainThread = Flow.newScheduler();

    Terminal terminal = TerminalBuilder.terminal();
    terminal.enterRawMode();

    Publisher<KeyPress> input = Flow.of(mainThread.receiving(terminal.input()))
        .as(KeyPress::new)
        .build();

    // TODO register a WINCH signal handler -> window takes a Publisher<Size>.
    Size terminalSize = terminal.getSize();
    Window window = new Window(input, terminalSize.getRows(), terminalSize.getColumns(), stroke -> { });

    Console console = new Console(window, mainThread.ticking());
    console.doOnComplete(mainThread::shutdown);
    mainThread.startup(console::start);
  }
}
