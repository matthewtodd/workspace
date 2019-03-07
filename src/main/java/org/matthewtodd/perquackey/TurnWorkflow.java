package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, WordList>, TurnScreen.Events {
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen;
  private final Scorer scorer;
  private final Timer timer;
  private final Turn turn;
  private final Input input;
  private Publisher<Long> ticker;

  public TurnWorkflow(Publisher<Long> ticker) {
    this.ticker = ticker;

    screen = Flow.pipe();
    input = new Input();
    scorer = new Scorer();
    timer = new Timer(180L);
    turn = new Turn();
  }

  @Override public void start(Void ignored) {
    Flow.of(ticker).subscribe(timer::tick);
    Flow.of(input.words()).subscribe(turn::spell);

    screen.onNext(new TurnScreen(
        Flow.of(turn.words(), Flow.of(turn.words()).as(scorer::score).build(), timer.state(),
            input.state()).as(TurnScreen.Data::new).build(), this));
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<WordList> result() {
    return Flow.of(turn.words()).last();
  }

  @Override public void letter(char letter) {
    input.letter(letter);
  }

  @Override public void undoLetter() {
    input.undoLetter();
  }

  @Override public void word() {
    input.word();
  }

  @Override public void toggleTimer() {
    timer.toggle();
  }

  @Override public void quit() {
    turn.quit();
    screen.onComplete();
  }
}
