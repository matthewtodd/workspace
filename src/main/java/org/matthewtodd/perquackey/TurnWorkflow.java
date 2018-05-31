package org.matthewtodd.perquackey;

import io.reactivex.Flowable;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class TurnWorkflow implements Workflow<Void, Turn.Snapshot>, SpellingScreen.Events, PausedScreen.Events {
  private final Turn turn;

  public TurnWorkflow(Timer timer) {
    turn = new Turn(timer);
  }

  @Override public void start(Void input) {
    turn.startTimer();
  }

  @Override public Publisher<WorkflowScreen<?, ?>> screen() {
    return Flowable.fromPublisher(turn.snapshot())
        .map(this::screenKeyFor)
        .distinctUntilChanged()
        .map(this::screenFor);
  }

  @Override public Publisher<Turn.Snapshot> result() {
    return Flowable.fromPublisher(turn.snapshot()).lastOrError().toFlowable();
  }

  @Override public void spell(String word) {
    turn.spell(word);
  }

  @Override public void pauseTimer() {
    turn.stopTimer();
  }

  @Override public void resumeTimer() {
    turn.startTimer();
  }

  private String screenKeyFor(Turn.Snapshot turnSnapshot) {
    return turnSnapshot.timer().running() ? SpellingScreen.KEY : PausedScreen.KEY;
  }

  private WorkflowScreen<?, ?> screenFor(String key) {
    switch (key) {
      case SpellingScreen.KEY:
        return new SpellingScreen(turn.snapshot(), this);
      case PausedScreen.KEY:
        return new PausedScreen(turn.snapshot(), this);
      default:
        throw new RuntimeException(String.format("Unrecognized key: %s", key));
    }
  }
}
