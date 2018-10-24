package org.matthewtodd.perquackey.console;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
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
  private final BlockingScheduler mainThread;
  private final TurnWorkflow workflow;
  private final Window window;

  private Console() {
    mainThread = new BlockingScheduler();
    workflow = new TurnWorkflow(new Timer(180, Flowable.interval(1, TimeUnit.SECONDS).observeOn(mainThread)));
    window = new Window(mainThread);
  }

  private void run() {
    Disposable subscription = Flowable.fromPublisher(workflow.screen())
        .map(this::viewFactory)
        .subscribe(window::setContents);

    mainThread.execute(() -> workflow.start(null));

    subscription.dispose();
  }

  private View viewFactory(WorkflowScreen<?, ?> screen) {
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
    private View currentView = new View(Coordinator.NONE);

    Window(Scheduler mainThread) {
      Scanner keyboard = new Scanner(System.in);
      Flowable.<String>generate(emitter -> emitter.onNext(keyboard.next()))
          .subscribeOn(Schedulers.io())
          .observeOn(mainThread)
          .subscribe(input);
    }

    void setContents(View view) {
      currentView.detach();
      currentView = view;
      currentView.attach(this);
    }

    Publisher<String> input() {
      return input;
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
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    Publisher<String> input() {
      return window.input();
    }
  }

  private static class SpellingView extends View {
    SpellingView(Coordinator coordinator) {
      super(coordinator);
    }

    void timer(Timer.Snapshot timer) {
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    void words(Set<String> words) {
      System.out.println(words);
    }

    Publisher<String> input() {
      return window.input();
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
    new Console().run();
  }
}
