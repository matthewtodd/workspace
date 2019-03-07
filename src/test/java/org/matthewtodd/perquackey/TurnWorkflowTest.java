package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import org.junit.Test;
import org.matthewtodd.workflow.WorkflowTester;

import static org.assertj.core.api.Assertions.assertThat;

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

    WorkflowTester<Void, WordList> workflow =
        new WorkflowTester<>(new TurnWorkflow(new Timer(180, ticker)));

    workflow.start(null);

    workflow.on(TurnScreen.class, (data, events) -> {
      assertThat(data.get().words()).isEmpty();
      assertThat(data.get().score()).isEqualTo(0);
      assertThat(data.get().timer().running()).isFalse();
      events.toggleTimer();
      assertThat(data.get().timer().running()).isTrue();
      events.letter('d');
      events.letter('o');
      events.letter('g');
      events.word();
      assertThat(data.get().words()).containsExactly("dog");
      assertThat(data.get().score()).isEqualTo(60);
    });

    for (int i = 0; i < 180; i++) {
      ticker.onNext(1L);
    }

    workflow.on(TurnScreen.class, (data, events) -> events.quit());

    assertThat(workflow.result()).containsExactly("dog");
  }
}
