package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import static org.matthewtodd.perquackey.Announcement.TimeIsUp;

public class TurnWorkflow implements Workflow<Boolean, Integer>, TurnScreen.Events {
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen;
  private final Processor<Integer, Integer> result;
  private final List<Runnable> cancelHooks;
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

    cancelHooks = new ArrayList<>();
    screen = Flow.pipe();
    result = Flow.pipe();

    dice = new Dice();
    dictionary = Dictionary.standard();
    scorer = new Scorer();
    timer = new Timer(180L);
    words = new Words();
    input = new Input(word ->
        words.lengthOkay(word) && dice.couldSpell(word) && dictionary.contains(word));
  }

  @Override public void start(final Boolean vulnerable) {
    dice.setVulnerable(vulnerable);
    words.setVulnerable(vulnerable);

    Flow.of(ticker).subscribe(timer::tick, cancelHooks::add);
    Flow.of(input.entries()).subscribe(words::spell, cancelHooks::add);
    Flow.of(words.state()).subscribe(dice::observe, cancelHooks::add);
    Flow.of(Flow.of(timer.state()).last()).subscribe(s -> announcer.accept(TimeIsUp), cancelHooks::add);
    Flow.of(words.state())
        .as(scorer::score)
        .as(score -> (vulnerable && score < 1000) ? -500 : score)
        .last()
        .subscribe(result);

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

  @Override public Publisher<Integer> result() {
    return result;
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
    cancelHooks.forEach(Runnable::run);
    cancelHooks.clear();
    words.quit();
    screen.onComplete();
  }
}
