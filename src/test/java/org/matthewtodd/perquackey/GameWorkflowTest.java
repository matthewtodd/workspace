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

  @Test public void hookup() {
    workflow.turn(turn -> {
      turn.assertInputIsEqualTo(false);
      turn.result(850);
    });

    workflow.summary(screen -> {
      screen.assertPlayers().containsExactly("Player 1");
      screen.assertPlayerScores("Player 1").containsExactly(850);
    });
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

    ListAssert<String> assertPlayers() {
      List<String> playerNames = new ArrayList<>();
      for (int i = 0; i < data.get().playerCount(); i++) {
        playerNames.add(data.get().playerName(i));
      }
      return assertThat(playerNames);
    }

    ListAssert<Integer> assertPlayerScores(String playerName) {
      // TODO honor player name
      return assertThat(data.get().playerScores(0));
    }

    void quit() {
      events.quit();
    }
  }
}