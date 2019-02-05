package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, Turn.Snapshot>, TurnScreen.Events {
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

  @Override public Publisher<Turn.Snapshot> result() {
    return Flow.of(turn.snapshot()).last();
  }

  @Override public void spell(String word) {
    turn.spell(word);
  }

  @Override public void toggleTimer() {
    turn.toggleTimer();
  }

  @Override public void quit() {
    turn.quit();
  }

  private String screenKeyFor(Turn.Snapshot snapshot) {
    return TurnScreen.KEY;
  }

  private WorkflowScreen<?, ?> screenFor(String key) {
    return new TurnScreen(turn.snapshot(), this);
  }
}
