package org.matthewtodd.perquackey.console;

import java.io.IOException;
import org.matthewtodd.console.Device;
import org.matthewtodd.console.HorizontalRule;
import org.matthewtodd.console.TableView;
import org.matthewtodd.console.TerminalDevice;
import org.matthewtodd.console.TextView;
import org.matthewtodd.console.View;
import org.matthewtodd.console.ViewGroup;
import org.matthewtodd.console.Window;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

import static org.matthewtodd.console.Alignment.LEFT;
import static org.matthewtodd.console.Alignment.RIGHT;
import static org.matthewtodd.console.ViewGroup.Orientation.COLUMNS;
import static org.matthewtodd.console.ViewGroup.Orientation.ROWS;

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
        return buildView(new PausedCoordinator((PausedScreen) screen));
      case SpellingScreen.KEY:
        return buildView(new SpellingCoordinator((SpellingScreen) screen));
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  private View buildView(Coordinator coordinator) {
    View view = new ViewGroup(ROWS,
        new ViewGroup(COLUMNS,
            new TextView("score", LEFT),
            new TextView("timer", RIGHT)),
        new HorizontalRule(),
        new TableView("words"),
        new HorizontalRule(),
        new TextView("input", LEFT));

    view.setAttachmentListener(coordinator::attach);
    view.setDetachmentListener(coordinator::detach);

    return view;
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
