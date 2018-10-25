package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import org.junit.Test;
import org.matthewtodd.workflow.WorkflowTester;

public class TurnWorkflowTest {
  @Test public void playingATurn() {
    FlowableProcessor<Long> ticker = BehaviorProcessor.create();

    WorkflowTester<Void, Turn.Snapshot> workflow =
        new WorkflowTester<>(new TurnWorkflow(new Timer(180, ticker)));

    workflow.start(null);

    workflow.on(SpellingScreen.class, screen -> {
      screen.assertThat(Turn.Snapshot::words).isEmpty();
      screen.assertThat(Turn.Snapshot::score).isEqualTo(0);
      screen.assertThat(Turn.Snapshot::timer).extracting(Timer.Snapshot::running).containsExactly(true);
      screen.send().spell("dog");
      screen.assertThat(Turn.Snapshot::words).containsExactly("dog");
      screen.assertThat(Turn.Snapshot::score).isEqualTo(60);
      screen.send().pauseTimer();
    });

    workflow.on(PausedScreen.class, screen -> {
      screen.assertThat(Turn.Snapshot::timer).extracting(Timer.Snapshot::running).containsExactly(false);
      screen.send().resumeTimer();
    });

    for (int i = 0; i < 180; i++) {
      ticker.onNext(1L);
    }

    workflow.assertThat(Turn.Snapshot::score).isEqualTo(60);
  }
}
