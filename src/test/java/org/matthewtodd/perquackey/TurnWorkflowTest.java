package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import org.junit.Test;
import org.matthewtodd.workflow.WorkflowTester;

public class TurnWorkflowTest {
  // This test isn't all that interesting --
  // terminal.PerquackeyTest does all the same things through the UI.
  // TurnTest gets into the nitty gritty of a turn.
  // What this all points to is that the TurnWorkflow is kind of anemic!
  // It just has one screen.
  // We will be growing it (and this test) to support playing through an entire game,
  // renaming to GameWorkflow, and then it will have a place.
  //
  // Thinking again, though, *this* is the platform-agnostic application/domain layer.
  // Maybe it's the best place to do most of our testing?
  // So, which tests belong at app, workflow, lego layers? Pyramid?
  @Test public void playingATurn() {
    FlowableProcessor<Long> ticker = BehaviorProcessor.create();

    WorkflowTester<Void, TurnScreen.Data> workflow =
        new WorkflowTester<>(new TurnWorkflow(new Timer(180, ticker)));

    workflow.start(null);

    workflow.on(TurnScreen.class, (data, events) -> {
      data.assertThat(TurnScreen.Data::words).isEmpty();
      data.assertThat(TurnScreen.Data::score).isEqualTo(0);
      data.assertThat(TurnScreen.Data::timer).extracting(Timer.Snapshot::running).containsExactly(false);
      events.toggleTimer();
      data.assertThat(TurnScreen.Data::timer).extracting(Timer.Snapshot::running).containsExactly(true);
      events.letter('d');
      events.letter('o');
      events.letter('g');
      events.word();
      data.assertThat(TurnScreen.Data::words).containsExactly("dog");
      data.assertThat(TurnScreen.Data::score).isEqualTo(60);
    });

    for (int i = 0; i < 180; i++) {
      ticker.onNext(1L);
    }

    workflow.on(TurnScreen.class, (data, events) -> {
      events.quit();
    });

    workflow.assertThat(TurnScreen.Data::score).isEqualTo(60);
  }
}
