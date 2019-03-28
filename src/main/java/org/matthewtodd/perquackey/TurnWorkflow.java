package org.matthewtodd.perquackey;

import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import static org.matthewtodd.perquackey.Announcement.TimeIsUp;

public class TurnWorkflow implements Workflow<Void, Words.State>, TurnScreen.Events {
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen;
  private final Scorer scorer;
  private final Timer timer;
  private final Words words;
  private final Input input;
  private final Publisher<Long> ticker;
  private final Consumer<Announcement> announcer;
  private final Dictionary dictionary;
  private final Dice dice;

  public TurnWorkflow(Publisher<Long> ticker, Consumer<Announcement> announcer) {
    this.ticker = ticker;
    this.announcer = announcer;

    screen = Flow.pipe();
    dice = new Dice();
    dictionary = Dictionary.standard();
    input = new Input(word -> dice.couldSpell(word) && dictionary.contains(word));
    scorer = new Scorer();
    timer = new Timer(180L);
    words = new Words();
  }

  @Override public void start(Void ignored) {
    Flow.of(ticker).subscribe(timer::tick);
    Flow.of(input.entries()).subscribe(words::spell);
    Flow.of(words.state()).subscribe(dice::observe);
    Flow.of(Flow.of(timer.state()).last()).subscribe(s -> announcer.accept(TimeIsUp));

    screen.onNext(new TurnScreen(Flow.of(
        words.state(),
        Flow.of(words.state()).as(scorer::score).build(),
        dice.state(),
        timer.state(),
        input.state()
    ).as(TurnScreen.Data::new).build(), this));
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<Words.State> result() {
    return Flow.of(words.state()).last();
  }

  @Override public void letter(char letter) {
    input.letter(letter);
  }

  @Override public void undoLetter() {
    input.undoLetter();
  }

  @Override public void word() {
    input.enter();
  }

  @Override public void toggleTimer() {
    timer.toggle();
  }

  @Override public void quit() {
    words.quit();
    screen.onComplete();
  }
}
