package org.matthewtodd.perquackey.console;

import java.io.IOException;
import java.util.function.Consumer;
import org.matthewtodd.console.AdapterView;
import org.matthewtodd.console.ConstraintLayout;
import org.matthewtodd.console.Device;
import org.matthewtodd.console.HorizontalRule;
import org.matthewtodd.console.TerminalDevice;
import org.matthewtodd.console.TextField;
import org.matthewtodd.console.TextView;
import org.matthewtodd.console.View;
import org.matthewtodd.console.Window;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.perquackey.Timer;
import org.matthewtodd.perquackey.TurnWorkflow;
import org.reactivestreams.Publisher;

import static org.matthewtodd.console.ConstraintLayout.Constraint.constrain;

public class Perquackey {
  static Builder newBuilder() {
    return new Builder();
  }

  // Interesting, this smells like a Guice module.
  static class Builder {
    private Publisher<Long> ticker;
    private Publisher<Integer> input;
    private Consumer<Runnable> scheduler;
    private Device device;

    Builder ticker(Publisher<Long> ticker) {
      this.ticker = ticker;
      return this;
    }

    Builder input(Publisher<Integer> input) {
      this.input = input;
      return this;
    }

    Builder scheduler(Consumer<Runnable> scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    Builder device(Device device) {
      this.device = device;
      return this;
    }

    Application build() {
      return new Application(workflow(), viewFactory(), window());
    }

    private TurnWorkflow workflow() {
      return new TurnWorkflow(new Timer(180L, ticker));
    }

    private ViewFactory viewFactory() {
      return ViewFactory.newBuilder()
          .view(turnView())
          .bind(PausedScreen.KEY, "turn", PausedCoordinator::new)
          .bind(SpellingScreen.KEY, "turn", SpellingCoordinator::new)
          .build();
    }

    private Window window() {
      return new Window(input, scheduler, device);
    }

    private View turnView() {
      return new AdapterView("turn",
          // However we express this, I'm imagining a set of constraints per view, used to arrange a graph of views in dependency order.
          new ConstraintLayout(
              constrain("score").top().toTopOfParent(),
              constrain("score").left().toLeftOfParent(),
              constrain("score").width().selfDetermined(),
              constrain("score").height().fixed(1),

              constrain("timer").top().toTopOfParent(),
              constrain("timer").right().toRightOfParent(),
              constrain("timer").width().selfDetermined(),
              constrain("timer").height().fixed(1),

              constrain("rule1").top().toBottomOf("score"),
              constrain("rule1").left().toLeftOfParent(),
              constrain("rule1").right().toRightOfParent(),
              constrain("rule1").height().fixed(1),

              constrain("words").top().toBottomOf("rule1"),
              constrain("words").left().toLeftOfParent(),
              constrain("words").right().toRightOfParent(),
              constrain("words").bottom().toTopOf("rule2"),

              constrain("rule2").bottom().toTopOf("input"),
              constrain("rule2").left().toLeftOfParent(),
              constrain("rule2").right().toRightOfParent(),
              constrain("rule2").height().fixed(1),

              constrain("input").bottom().toBottomOfParent(),
              constrain("input").left().toLeftOfParent(),
              constrain("input").width().selfDetermined(),
              constrain("input").height().fixed(1)
          ),

          AdapterView.staticChildren(
              new TextView("score"),
              new TextView("timer"),
              new HorizontalRule("rule1", '-'),
              //new AdapterView("words", Layout.table(), Collections::emptyList),
              new HorizontalRule("words", 'w'),
              new HorizontalRule("rule2", '-'),
              new TextField("input")
          ));
    }
  }

  public static void main(String[] args) throws IOException {
    Flow.Scheduler mainThread = Flow.newScheduler();
    TerminalDevice terminal = TerminalDevice.instance();

    Perquackey.newBuilder()
        .ticker(mainThread.ticking())
        .input(mainThread.receiving(terminal.input()))
        .scheduler(mainThread)
        .device(terminal)
        .build()
        .run(mainThread);
  }
}
