package org.matthewtodd.perquackey.console;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

  Console(Flowable<String> input, Flowable<Long> ticker, PrintStream output) {
    subscriptions = new CompositeDisposable();
    window = new Window(input, output);
    workflow = new TurnWorkflow(new Timer(180, ticker));
  }

  void start() {
    subscriptions.addAll(
        Flowable.fromPublisher(workflow.screen())
            .map(this::buildViewForScreen)
            .subscribe(window::displayView),

        Completable.fromPublisher(workflow.result())
            .subscribe(subscriptions::dispose));

    workflow.start(null);
  }

  private void doOnComplete(Runnable onComplete) {
    subscriptions.add(Disposables.fromRunnable(onComplete));
  }

  private View buildViewForScreen(WorkflowScreen<?, ?> screen) {
    // this maybe becomes a loading cache?
    switch (screen.key) {
      case PausedScreen.KEY:
        return new PausedView(new PausedCoordinator(screen));
      case SpellingScreen.KEY:
        return new SpellingView(new SpellingCoordinator(screen));
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  private static class Window {
    private final FlowableProcessor<String> input = PublishProcessor.create();
    private final PrintStream output;
    private View currentView = new View(Coordinator.NONE);

    Window(Flowable<String> stdin, PrintStream output) {
      stdin.subscribe(input);
      this.output = output;
    }

    void displayView(View view) {
      currentView.detach();
      currentView = view;
      currentView.attach(this);
    }
  }

  private static class View {
    final Coordinator coordinator;
    Window window;

    View(Coordinator coordinator) {
      this.coordinator = coordinator;
    }

    final void attach(Window window) {
      this.window = window;
      coordinator.attach(this);
    }

    final void detach() {
      coordinator.detach(this);
      this.window = null;
    }
  }

  private static class PausedView extends View {
    PausedView(Coordinator coordinator) {
      super(coordinator);
    }

    void timer(Timer.Snapshot timer) {
      window.output.printf("%s, %s\n", timer, Thread.currentThread());
    }

    Publisher<String> input() {
      return window.input;
    }
  }

  private static class SpellingView extends View {
    SpellingView(Coordinator coordinator) {
      super(coordinator);
    }

    void timer(Timer.Snapshot timer) {
      window.output.printf("%s, %s\n", timer, Thread.currentThread());
    }

    void words(Set<String> words) {
      window.output.println(words);
    }

    Publisher<String> input() {
      return window.input;
    }
  }

  private interface Coordinator {
    Coordinator NONE = new Coordinator() {
      @Override public void attach(View view) { }
      @Override public void detach(View view) { }
    };

    void attach(View view);

    void detach(View view);
  }

  private static class PausedCoordinator implements Coordinator {
    private final PausedScreen screen;
    private final CompositeDisposable subscription = new CompositeDisposable();

    PausedCoordinator(WorkflowScreen<?, ?> screen) {
      this.screen = (PausedScreen) screen;
    }

    @Override public void attach(View view) {
      PausedView pausedView = (PausedView) view;

      subscription.add(Flowable.fromPublisher(screen.screenData)
          .subscribe(turn -> pausedView.timer(turn.timer())));

      subscription.add(Flowable.fromPublisher(pausedView.input())
          .subscribe(ignored -> screen.eventHandler.resumeTimer()));
    }

    @Override public void detach(View view) {
      subscription.dispose();
    }
  }

  private static class SpellingCoordinator implements Coordinator {
    private final SpellingScreen screen;
    private final CompositeDisposable subscription = new CompositeDisposable();

    SpellingCoordinator(WorkflowScreen<?, ?> screen) {
      this.screen = (SpellingScreen) screen;
    }

    @Override public void attach(View view) {
      SpellingView spellingView = (SpellingView) view;

      subscription.add(Flowable.fromPublisher(screen.screenData)
          .subscribe(turn -> {
            spellingView.timer(turn.timer());
            spellingView.words(turn.words());
          }));

      subscription.add(Flowable.fromPublisher(spellingView.input())
          .subscribe(screen.eventHandler::spell));
    }

    @Override public void detach(View view) {
      subscription.dispose();
    }
  }

  public static void main(String[] args) {
    BlockingScheduler mainThread = new BlockingScheduler();

    Scanner scanner = new Scanner(System.in);

    Flowable<String> input =
        Flowable.<String>generate(emitter -> emitter.onNext(scanner.next()))
            .subscribeOn(Schedulers.io())
            .observeOn(mainThread);

    Flowable<Long> ticker = Flowable.interval(1, TimeUnit.SECONDS)
        .observeOn(mainThread);

    Console console = new Console(input, ticker, System.out);
    console.doOnComplete(mainThread::shutdown);
    mainThread.execute(console::start);
  }
}
