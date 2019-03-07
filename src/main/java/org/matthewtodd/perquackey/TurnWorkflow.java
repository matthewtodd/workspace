package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, WordList>, TurnScreen.Events {
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen;
  private final Timer timer;
  private final Turn turn;

  public TurnWorkflow(Timer timer) {
    this.screen = Flow.pipe();
    this.timer = timer;
    this.turn = new Turn();
  }

  @Override public void start(Void input) {
    Scorer scorer = new Scorer();
    Publisher<Integer> score = Flow.of(turn.words()).as(scorer::score).build();

    Publisher<TurnScreen.Data> data = Flow.of(turn.words(), score, timer.state(), turn.input())
            .as(TurnScreen.Data::new)
            .build();

    screen.onNext(new TurnScreen(data, this));
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<WordList> result() {
    return Flow.of(turn.words()).last();
  }

  @Override public void letter(char letter) {
    turn.letter(letter);
  }

  @Override public void undoLetter() {
    turn.undoLetter();
  }

  @Override public void word() {
    turn.word();
  }

  @Override public void toggleTimer() {
    timer.toggle();
  }

  @Override public void quit() {
    turn.quit();
    screen.onComplete();
  }
}
