package org.matthewtodd.perquackey.console;

import java.io.IOException;
import org.matthewtodd.console.Device;
import org.matthewtodd.console.TerminalDevice;
import org.matthewtodd.console.View;
import org.matthewtodd.console.Window;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Console {
  private final Window window;
  private final TurnWorkflow workflow;

  Console(Publisher<Long> ticker, Publisher<Integer> input, Device device) {
    this.window = new Window(input, device);
    this.workflow = new TurnWorkflow(new Timer(180L, ticker));
  }

  void start(Runnable onComplete) {
    Flow.of(workflow.screen())
        .subscribe(screen -> window.rootView(buildViewForScreen(screen)));

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(turn -> window.close());

    workflow.start(null);
  }

  private View buildViewForScreen(WorkflowScreen<?, ?> screen) {
    switch (screen.key) {
      case PausedScreen.KEY:
        PausedCoordinator pausedCoordinator = new PausedCoordinator((PausedScreen) screen);
        return new TurnView()
            .setAttachmentListener(pausedCoordinator::attach)
            .setDetachmentListener(pausedCoordinator::detach);
      case SpellingScreen.KEY:
        SpellingCoordinator spellingCoordinator = new SpellingCoordinator((SpellingScreen) screen);
        return new TurnView()
            .setAttachmentListener(spellingCoordinator::attach)
            .setDetachmentListener(spellingCoordinator::detach);
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  public static void main(String[] args) throws IOException {
    Flow.Scheduler mainThread = Flow.newScheduler();

    TerminalDevice terminal = TerminalDevice.instance();

    Console console = new Console(
        mainThread.ticking(),
        mainThread.receiving(terminal.input()),
        terminal);

    mainThread.start(() -> console.start(mainThread::shutdown));
  }
}
