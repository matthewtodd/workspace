package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class GameWorkflow implements Workflow<Void, String>, SummaryScreen.Events {
  private final Supplier<Workflow<Boolean, Integer>> factory;
  private final List<Integer> scores;
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen = Flow.pipe();
  private final Processor<String, String> result = Flow.pipe();

  public GameWorkflow(Supplier<Workflow<Boolean, Integer>> factory) {
    this.factory = factory;
    this.scores = new ArrayList<>();
  }

  @Override public void start(Void input) {
    nextTurn();
  }

  @Override public Publisher<? extends WorkflowScreen<?, ?>> screen() {
    return screen;
  }

  @Override public Publisher<String> result() {
    return result;
  }

  @Override public void nextTurn() {
    Workflow<Boolean, Integer> turn = factory.get();
    Flow.of(turn.screen()).subscribe(screen::onNext);
    Flow.of(turn.result()).subscribe(this::turnComplete);
    turn.start(scores.stream().mapToInt(s -> s).sum() >= 2000);
  }

  @Override public void quit() {
    screen.onComplete();
    result.onNext("");
    result.onComplete();
  }

  private void turnComplete(Integer score) {
    scores.add(score);
    screen.onNext(new SummaryScreen(Flow.pipe(new SummaryScreen.Data(scores)), this));
  }
}
