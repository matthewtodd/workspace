package org.matthewtodd.perquackey;

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
    workflow.summary(score -> score.assertScores().containsExactly(850));
  }

  @Test public void anotherTurn() {
    workflow.turn(turn -> turn.result(850));
    workflow.summary(SummaryScreenTester::nextTurn);
    workflow.turn(turn -> turn.result(3000));
    workflow.summary(score -> score.assertScores().containsExactly(850, 3000));
  }

  @Test public void vulnerability_notVulnerable() {
    workflow.turn(turn -> turn.result(850));
    workflow.summary(SummaryScreenTester::nextTurn);
    workflow.turn(turn -> turn.assertInputIsEqualTo(false));
  }

  @Test public void vulnerability_vulnerable() {
    workflow.turn(turn -> turn.result(2000));
    workflow.summary(SummaryScreenTester::nextTurn);
    workflow.turn(turn -> turn.assertInputIsEqualTo(true));
  }

  @Test public void quitting() {
    workflow.turn(turn -> turn.result(0));
    workflow.summary(SummaryScreenTester::quit);
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

    void summary(Consumer<SummaryScreenTester> assertions) {
      workflow.on(SummaryScreen.class,
          (data, events) -> assertions.accept(new SummaryScreenTester(data, events)));
    }

    AbstractStringAssert<?> assertThatResult() {
      return assertThat(workflow.result());
    }
  }

  static class SummaryScreenTester {
    private final AssertSubscriber<SummaryScreen.Data> data;
    private final SummaryScreen.Events events;

    SummaryScreenTester(AssertSubscriber<SummaryScreen.Data> data, SummaryScreen.Events events) {
      this.data = data;
      this.events = events;
    }

    ListAssert<Integer> assertScores() {
      return assertThat(data.get().playerScores(0).subList(0, data.get().scoreCount()));
    }

    void nextTurn() {
      events.nextTurn();
    }

    void quit() {
      events.quit();
    }
  }
}