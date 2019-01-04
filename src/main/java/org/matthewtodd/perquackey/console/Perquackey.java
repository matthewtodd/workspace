package org.matthewtodd.perquackey.console;

import java.io.IOException;
import java.util.function.Consumer;
import org.matthewtodd.console.AdapterView;
import org.matthewtodd.console.ConstraintLayout;
import org.matthewtodd.console.Device;
import org.matthewtodd.console.HorizontalRule;
import org.matthewtodd.console.TerminalDevice;
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
  static Builder with(Publisher<Long> ticker) {
    return new Builder().ticker(ticker);
  }

  // Interesting, this smells like a Guice module.
  // Still playing with boundaries.
  // There's almost a reusable thing here, like -> workflow, -> window, -> viewFactory, -> app.
  static class Builder {
    private TurnWorkflow workflow;
    private Window window;

    Builder ticker(Publisher<Long> ticker) {
      workflow = new TurnWorkflow(new Timer(180L, ticker));
      return this;
    }

    Application on(Publisher<Integer> input, Consumer<Runnable> scheduler, Device device) {
      window = new Window(input, scheduler, device);
      return build();
    }

    private Application build() {
      return new Application(workflow, viewFactory(), window);
    }

    private ViewFactory viewFactory() {
      return ViewFactory.newBuilder(this::findViewById)
          .bind(PausedScreen.KEY, "turn", PausedCoordinator::new)
          .bind(SpellingScreen.KEY, "turn", SpellingCoordinator::new)
          .build();
    }

    private View findViewById(String id) {
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
              new TextView("input")
          ));
    }
  }

  public static void main(String[] args) throws IOException {
    Flow.Scheduler mainThread = Flow.newScheduler();
    TerminalDevice terminal = TerminalDevice.instance();

    Perquackey.with(mainThread.ticking())
        .on(mainThread.receiving(terminal.input()), mainThread, terminal)
        .run(mainThread);
  }
}
