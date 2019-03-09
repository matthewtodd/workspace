package org.matthewtodd.perquackey;

import java.util.function.Predicate;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, Words.State>, TurnScreen.Events {
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen;
  private final Scorer scorer;
  private final Timer timer;
  private final Words words;
  private final Input input;
  private final Publisher<Long> ticker;
  private final Predicate<String> dictionary;

  public TurnWorkflow(Publisher<Long> ticker) {
    this.ticker = ticker;

    screen = Flow.pipe();
    dictionary = word -> word.length() >= 3;
    input = new Input(dictionary);
    scorer = new Scorer();
    timer = new Timer(180L);
    words = new Words();
  }

  @Override public void start(Void ignored) {
    Flow.of(ticker).subscribe(timer::tick);
    Flow.of(input.entries()).subscribe(words::spell);

    screen.onNext(new TurnScreen(
        Flow.of(words.state(), Flow.of(words.state()).as(scorer::score).build(), timer.state(),
            input.state()).as(TurnScreen.Data::new).build(), this));
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
