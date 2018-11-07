package org.matthewtodd.perquackey.console;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.matthewtodd.console.Canvas;
import org.matthewtodd.console.Coordinator;
import org.matthewtodd.console.View;
import org.matthewtodd.console.Window;
import org.matthewtodd.console.Window.KeyPress;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Console {
  private final CompositeDisposable subscriptions;
  private final Window window;
  private final TurnWorkflow workflow;

  Console(Window window, Publisher<Long> ticker) {
    this.subscriptions = new CompositeDisposable();
    this.window = window;
    this.workflow = new TurnWorkflow(new Timer(180L, ticker));
  }

  void start() {
    subscriptions.addAll(
        Flowable.fromPublisher(workflow.screen())
            .map(this::buildViewForScreen)
            .subscribe(window::rootView),

        Flowable.fromPublisher(workflow.result())
            .ignoreElements()
            .subscribe(subscriptions::dispose));

    workflow.start(null);
  }

  private void doOnComplete(Runnable onComplete) {
    subscriptions.add(
        Disposables.fromRunnable(onComplete));
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
    private final CompositeDisposable subscription = new CompositeDisposable();

    PausedCoordinator(PausedScreen screen) {
      this.screen = screen;
    }

    @Override public void attach(PausedView view) {
      subscription.add(Flowable.fromPublisher(screen.screenData)
          .subscribe(turn -> view.timer(turn.timer())));

      view.setKeyPressListener(keyPress -> {
        if (keyPress.isSpaceBar()) {
          screen.eventHandler.resumeTimer();
        }
      });
    }

    @Override public void detach() {
      subscription.dispose();
    }
  }

  private static class SpellingCoordinator implements Coordinator<SpellingView> {
    private final SpellingScreen screen;
    private final CompositeDisposable subscription = new CompositeDisposable();
    private final StringBuilder buffer;

    SpellingCoordinator(SpellingScreen screen) {
      this.screen = screen;
      this.buffer = new StringBuilder();
    }

    @Override public void attach(SpellingView view) {
      subscription.add(Flowable.fromPublisher(screen.screenData)
          .subscribe(turn -> {
            view.timer(turn.timer());
            view.words(turn.words());
          }));

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

    @Override public void detach() {
      subscription.dispose();
    }
  }

  public static void main(String[] args) throws IOException {
    BlockingScheduler mainThread = new BlockingScheduler();

    Terminal terminal = TerminalBuilder.terminal();
    terminal.enterRawMode();

    Flowable<KeyPress> input =
        Flowable.<KeyPress>generate(emitter -> emitter.onNext(new KeyPress(terminal.input().read())))
            .subscribeOn(Schedulers.io())
            .observeOn(mainThread);

    // TODO register a WINCH signal handler.
    Size terminalSize = terminal.getSize();
    Window window = new Window(input, terminalSize.getRows(), terminalSize.getColumns(), stroke -> { });

    Flowable<Long> ticker = Flowable.interval(1, TimeUnit.SECONDS)
        .observeOn(mainThread);

    Console console = new Console(window, ticker);
    console.doOnComplete(mainThread::shutdown);
    mainThread.execute(console::start);
  }
}
