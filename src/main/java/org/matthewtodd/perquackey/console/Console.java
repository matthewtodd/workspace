package org.matthewtodd.perquackey.console;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class Console {
  private final TurnWorkflow workflow;
  private final ViewFactory viewFactory;

  public Console() {
    workflow = new TurnWorkflow(new Timer(5, Flowable.interval(1, TimeUnit.SECONDS)));
    viewFactory = new ViewFactory(System.in, System.out);
  }

  public void run() {
    Flowable.fromPublisher(workflow.screen())
        .subscribe(viewFactory);

    workflow.start(null);

    // I need some kind of event run loop now, so the program doesn't exit early...

    System.out.println("Done!");
  }

  private static class ViewFactory implements Subscriber<WorkflowScreen<?, ?>> {
    private final InputStream in;
    private final PrintStream out;
    private Coordinator current;

    public ViewFactory(InputStream in, PrintStream out) {
      this.in = in;
      this.out = out;
    }

    @Override public void onSubscribe(Subscription s) {
      s.request(Long.MAX_VALUE);
    }

    @Override public void onNext(WorkflowScreen<?, ?> screen) {
      out.printf("--- new screen! %s\n", screen);
      detachCurrent();

      switch (screen.key) {
        case PausedScreen.KEY:
          current = new PausedCoordinator((PausedScreen) screen);
          break;
        case SpellingScreen.KEY:
          current = new SpellingCoordinator((SpellingScreen) screen);
          break;
        default:
          throw new IllegalStateException(String.format("Unknown screen: %s", screen.key));
      }

      current.attach(in, out);
    }

    @Override public void onError(Throwable t) {

    }

    @Override public void onComplete() {
      detachCurrent();
    }

    private void detachCurrent() {
      if (current != null) {
        current.detach(in, out);
      }
    }
  }

  private interface Coordinator {
    void attach(InputStream in, PrintStream out);
    void detach(InputStream in, PrintStream out);
  }

  private static class PausedCoordinator implements Coordinator {
    private final PausedScreen screen;
    private Disposable subscription;

    PausedCoordinator(PausedScreen screen) {
      this.screen = screen;
    }

    @Override public void attach(InputStream in, PrintStream out) {
      out.printf("Attaching %s\n", this);
      subscription = Flowable.fromPublisher(screen.screenData).subscribe(turn -> {
        out.println(turn);
        out.println(turn.timer());
        out.println(turn.score());
        out.println(turn.words());
        out.println("resume: ");

        //String next = in.next();
        //screen.eventHandler.resumeTimer();
      });
    }

    @Override public void detach(InputStream in, PrintStream out) {
      subscription.dispose();
      out.printf("Detached %s\n", this);
    }
  }

  private static class SpellingCoordinator implements Coordinator {
    private final SpellingScreen screen;
    private Disposable subscription;

    public SpellingCoordinator(SpellingScreen screen) {
      this.screen = screen;
    }

    @Override public void attach(InputStream in, PrintStream out) {
      out.printf("Attaching %s\n", this);
      subscription = Flowable.fromPublisher(screen.screenData).subscribe(turn -> {
        out.println(turn.timer());
        out.println(turn.score());
        out.println(turn.words());
        out.println("word: ");

        //String next = in.next();
        //switch (next) {
        //  case "":
        //    screen.eventHandler.pauseTimer();
        //    break;
        //  default:
        //    screen.eventHandler.spell(next);
        //}
      });
    }

    @Override public void detach(InputStream in, PrintStream out) {
      subscription.dispose();
      out.printf("Detached %s\n", this);
    }
  }

  public static void main(String[] args) {
    new Console().run();
  }
}
