package org.matthewtodd.perquackey.console;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class Console {
  private final TurnWorkflow workflow;
  private final Window window;

  private Console() {
    workflow = new TurnWorkflow(new Timer(3, Flowable.interval(1, TimeUnit.SECONDS)));
    window = new Window(new BlockingScheduler());
  }

  private void run() {
    window.scheduler.execute(() -> {
      Disposable subscribe = Flowable.fromPublisher(workflow.screen())
          .observeOn(window.scheduler)
          .map(this::viewFactory)
          .subscribe(window::setContents);
      workflow.start(null);
    });
  }

  private View viewFactory(WorkflowScreen<?, ?> screen) {
    switch (screen.key) {
      case PausedScreen.KEY:
        return new PausedView(new PausedCoordinator(screen, window.scheduler));
      case SpellingScreen.KEY:
        return new SpellingView(new SpellingCoordinator(screen, window.scheduler));
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  private static class Window {
    private final BlockingScheduler scheduler;
    private View currentView = new View(Coordinator.NONE);

    Window(BlockingScheduler scheduler) {
      this.scheduler = scheduler;
    }

    void setContents(View view) {
      currentView.detach();
      currentView = view;
      currentView.attach(this);
    }
  }

  private static class View {
    private final Coordinator coordinator;

    View(Coordinator coordinator) {
      this.coordinator = coordinator;
    }

    final void attach(Window window) {
      coordinator.attach(this);
    }

    final void detach() {
      coordinator.detach(this);
    }
  }

  private static class PausedView extends View {
    PausedView(Coordinator coordinator) {
      super(coordinator);
    }

    void timer(Timer.Snapshot timer) {
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    // Maybe?
    // https://github.com/shekhargulati/rxjava-examples/blob/master/src/main/java/org/shekhar/rxjava/examples/KeyboardObservableExample.java
    Publisher<String> input() {
      return null;
    }
  }

  private static class SpellingView extends View {
    SpellingView(Coordinator coordinator) {
      super(coordinator);
    }

    void timer(Timer.Snapshot timer) {
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    Publisher<String> input() {
      return null;
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
    private final Scheduler scheduler;
    private final CompositeDisposable subscription = new CompositeDisposable();

    PausedCoordinator(WorkflowScreen<?, ?> screen, Scheduler scheduler) {
      this.screen = (PausedScreen) screen;
      this.scheduler = scheduler;
    }

    @Override public void attach(View view) {
      PausedView pausedView = (PausedView) view;

      subscription.add(Flowable.fromPublisher(screen.screenData)
          .observeOn(scheduler)
          .subscribe(turn -> pausedView.timer(turn.timer())));

      //subscription.add(Completable.fromPublisher(pausedView.input())
      //    .subscribe(screen.eventHandler::resumeTimer));
    }

    @Override public void detach(View view) {
      subscription.dispose();
    }
  }

  private static class SpellingCoordinator implements Coordinator {
    private final SpellingScreen screen;
    private final Scheduler scheduler;
    private final CompositeDisposable subscription = new CompositeDisposable();

    SpellingCoordinator(WorkflowScreen<?, ?> screen, Scheduler scheduler) {
      this.screen = (SpellingScreen) screen;
      this.scheduler = scheduler;
    }

    @Override public void attach(View view) {
      SpellingView spellingView = (SpellingView) view;

      subscription.add(Flowable.fromPublisher(screen.screenData)
          .observeOn(scheduler)
          .subscribe(turn -> spellingView.timer(turn.timer())));

      //subscription.add(Flowable.fromPublisher(spellingView.input())
      //    .subscribe(screen.eventHandler::spell));
    }

    @Override public void detach(View view) {
      subscription.dispose();
    }
  }

  public static void main(String[] args) {
    new Console().run();
  }
}
