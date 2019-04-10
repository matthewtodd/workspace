package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

public class GameWorkflow implements Workflow<Void, String>, ScorecardScreen.Events {
  private final Supplier<Workflow<Boolean, Integer>> factory;
  private final Scorecard scorecard;
  private final Processor<WorkflowScreen<?, ?>, WorkflowScreen<?, ?>> screen = Flow.pipe();
  private final Processor<String, String> result = Flow.pipe();

  public GameWorkflow(Supplier<Workflow<Boolean, Integer>> factory) {
    this.factory = factory;
    this.scorecard = new Scorecard();
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

  @Override public void numberOfPlayers(int numberOfPlayers) {
    scorecard.numberOfPlayers(numberOfPlayers);
  }

  @Override public void nextTurn() {
    Workflow<Boolean, Integer> turn = factory.get();
    Flow.of(turn.screen()).subscribe(screen::onNext);
    Flow.of(turn.result()).subscribe(this::turnComplete);
    turn.start(scorecard.currentScore() >= 2000);
  }

  @Override public void quit() {
    screen.onComplete();
    result.onNext("");
    result.onComplete();
  }

  private void turnComplete(int score) {
    scorecard.turnComplete(score);
    screen.onNext(new ScorecardScreen(scorecard.state(), this));
  }

  private static class Scorecard {
    private final List<List<Integer>> scores;
    private final Processor<ScorecardScreen.Data, ScorecardScreen.Data> state;
    private int currentPlayer = 0;

    private Scorecard() {
      scores = new ArrayList<>();
      scores.add(new ArrayList<>());
      state = Flow.pipe(new ScorecardScreen.Data(scores));
    }

    void numberOfPlayers(int numberOfPlayers) {
      if (numberOfPlayers < 1) {
        return;
      }

      while (numberOfPlayers < scores.size()) {
        scores.remove(scores.size() - 1);
        currentPlayer %= scores.size();
      }

      while (scores.size() < numberOfPlayers) {
        scores.add(new ArrayList<>());
        if (!scores.get(currentPlayer).isEmpty()) {
          currentPlayer = scores.size() - 1;
        }
      }

      state.onNext(new ScorecardScreen.Data(scores));
    }

    void turnComplete(int score) {
      scores.get(currentPlayer).add(score);
      currentPlayer = (currentPlayer + 1) % scores.size();
      state.onNext(new ScorecardScreen.Data(scores));
    }

    Publisher<ScorecardScreen.Data> state() {
      return state;
    }

    int currentScore() {
      return scores.get(currentPlayer).stream().mapToInt(s -> s).sum();
    }
  }
}
