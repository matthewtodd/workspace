package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;
import org.matthewtodd.workflow.StubWorkflow;
import org.matthewtodd.workflow.StubWorkflowScreen;
import org.matthewtodd.workflow.WorkflowTester;

import static org.assertj.core.api.Assertions.assertThat;

public class GameWorkflowTest {
  private GameWorkflowTester workflow;

  @Before public void setUp() {
    workflow = new GameWorkflowTester();
    workflow.start();
  }

  @Test public void scoring() {
    workflow.turn(turn -> turn.result(850));
    workflow.scorecard(score -> score.assertScores(0).containsExactly(850));
  }

  @Test public void anotherTurn() {
    workflow.turn(turn -> turn.result(850));
    workflow.scorecard(ScorecardScreenTester::nextTurn);
    workflow.turn(turn -> turn.result(3000));
    workflow.scorecard(score -> score.assertScores(0).containsExactly(850, 3000));
  }

  @Test public void twoPlayers() {
    workflow.turn(turn -> turn.result(850));
    workflow.scorecard(score -> {
      score.numberOfPlayers(2);
      score.nextTurn();
    });
    workflow.turn(turn -> turn.result(2100));
    workflow.scorecard(score -> {
      score.assertScores(0).containsExactly(850);
      score.assertScores(1).containsExactly(2100);
    });
  }

  @Test public void vulnerability_notVulnerable() {
    workflow.turn(turn -> turn.result(850));
    workflow.scorecard(ScorecardScreenTester::nextTurn);
    workflow.turn(turn -> turn.assertInputIsEqualTo(false));
  }

  @Test public void vulnerability_vulnerable() {
    workflow.turn(turn -> turn.result(2000));
    workflow.scorecard(ScorecardScreenTester::nextTurn);
    workflow.turn(turn -> turn.assertInputIsEqualTo(true));
  }

  @Test public void quitting() {
    workflow.turn(turn -> turn.result(0));
    workflow.scorecard(ScorecardScreenTester::quit);
    workflow.assertThatResult().isEqualTo("");
  }

  static class GameWorkflowTester {
    private final WorkflowTester<Void, String> workflow;

    GameWorkflowTester() {
      workflow = new WorkflowTester<>(new GameWorkflow(StubWorkflow::new));
    }

    void start() {
      workflow.start(null);
    }

    void turn(Consumer<StubWorkflowScreen.Events> assertions) {
      workflow.on(StubWorkflowScreen.class, (data, events) -> assertions.accept(events));
    }

    void scorecard(Consumer<ScorecardScreenTester> assertions) {
      workflow.on(ScorecardScreen.class,
          (data, events) -> assertions.accept(new ScorecardScreenTester(data, events)));
    }

    AbstractStringAssert<?> assertThatResult() {
      return assertThat(workflow.result());
    }
  }

  static class ScorecardScreenTester {
    private final AssertSubscriber<ScorecardScreen.Data> data;
    private final ScorecardScreen.Events events;

    ScorecardScreenTester(AssertSubscriber<ScorecardScreen.Data> data, ScorecardScreen.Events events) {
      this.data = data;
      this.events = events;
    }

    ListAssert<Integer> assertScores(int playerNumber) {
      List<Integer> scores = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < data.get().scoreCount(); rowIndex++) {
        scores.add(data.get().playerScore(playerNumber, rowIndex));
      }
      return assertThat(scores);
    }

    void numberOfPlayers(int numberOfPlayers) {
      events.numberOfPlayers(numberOfPlayers);
    }

    void nextTurn() {
      events.nextTurn();
    }

    void quit() {
      events.quit();
    }
  }
}