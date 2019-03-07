package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, TurnScreen.Data>, TurnScreen.Events {
  private final Turn turn;

  public TurnWorkflow(Timer timer) {
    turn = new Turn(timer);
  }

  @Override public void start(Void input) { }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return Flow.of(turn.snapshot())
        .as(this::screenKeyFor)
        .distinct()
        .as(this::screenFor)
        .build();
  }

  @Override public Publisher<TurnScreen.Data> result() {
    return Flow.of(turn.snapshot()).last();
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
    turn.toggleTimer();
  }

  @Override public void quit() {
    turn.quit();
  }

  private String screenKeyFor(TurnScreen.Data data) {
    return TurnScreen.KEY;
  }

  private WorkflowScreen<?, ?> screenFor(String key) {
    return new TurnScreen(turn.snapshot(), this);
  }
}
