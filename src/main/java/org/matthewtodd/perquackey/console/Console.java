package org.matthewtodd.perquackey.console;

import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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
    window = new Window();
  }

  private void run() {
    mainThread.execute(() -> {
      Disposable subscribe = Flowable.fromPublisher(workflow.screen())
          .map(this::viewFactory)
          .subscribe(window::setContents);
      workflow.start(null);
    });
  }

  private View viewFactory(WorkflowScreen<?, ?> screen) {
    switch (screen.key) {
      case PausedScreen.KEY:
        return new PausedView(new PausedCoordinator(screen), mainThread);
      case SpellingScreen.KEY:
        return new SpellingView(new SpellingCoordinator(screen), mainThread);
      default:
        throw new AssertionError(String.format("Unexpected screen: %s", screen));
    }
  }

  private static class Window {
    private View currentView = new View(Coordinator.NONE, null); // TODO find something nicer.

    void setContents(View view) {
      currentView.detach();
      currentView = view;
      currentView.attach();
    }
  }

  private static class View {
    final Coordinator coordinator;
    final Scheduler mainThread;

    View(Coordinator coordinator, Scheduler mainThread) {
      this.coordinator = coordinator;
      this.mainThread = mainThread;
    }

    final void attach() {
      coordinator.attach(this);
    }

    final void detach() {
      coordinator.detach(this);
    }
  }

  private static class PausedView extends View {
    PausedView(Coordinator coordinator, Scheduler mainThread) {
      super(coordinator, mainThread);
    }

    void timer(Timer.Snapshot timer) {
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    // I want to make this more general: the view and the coordinator shouldn't have to know
    // that this is a one-shot input that triggers a state transition.
    // But spinning over keyboard.next() on a (background) io thread over-eagerly consumes the
    // next token on stdin (we don't yet know the subscription has been disposed), so
    // we accidentally swallow the first spelled word.
    // Using a pattern here (passing one to next()) wouldn't help, because we'd already be
    // blocked in the while loop. So we'd either have to consume *something* we shouldn't or
    // block forever.
    // Perhaps there could be something in sharing the input "device" across views.
    // Maybe it's a property of the window? There's probably something to explore here.
    Publisher<String> input() {
      Scanner keyboard = new Scanner(System.in);
      return Flowable.<String>create(emitter -> {
        keyboard.next();
        emitter.onComplete();
      }, BackpressureStrategy.BUFFER)
          .subscribeOn(Schedulers.io())
          .observeOn(mainThread);
    }
  }

  private static class SpellingView extends View {
    SpellingView(Coordinator coordinator, Scheduler mainThread) {
      super(coordinator, mainThread);
    }

    void timer(Timer.Snapshot timer) {
      System.out.printf("%s, %s\n", timer, Thread.currentThread());
    }

    void words(Set<String> words) {
      System.out.println(words);
    }

    Publisher<String> input() {
      Scanner keyboard = new Scanner(System.in);
      return Flowable.<String>create(emitter -> {
        while (!emitter.isCancelled()) {
          emitter.onNext(keyboard.next());
        }
      }, BackpressureStrategy.BUFFER)
          .subscribeOn(Schedulers.io())
          .observeOn(mainThread);
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

      subscription.add(Completable.fromPublisher(pausedView.input())
          .subscribe(screen.eventHandler::resumeTimer));
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
