package org.matthewtodd.perquackey.console;

import org.matthewtodd.console.TextView;
import org.matthewtodd.console.View;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.workflow.WorkflowScreen;

class PausedCoordinator implements Coordinator {
  private final PausedScreen screen;

  PausedCoordinator(WorkflowScreen<?, ?> screen) {
    this.screen = (PausedScreen) screen;
  }

  @Override public void attach(View view) {
    Flow.of(screen.screenData).subscribe(turn -> {
      view.find("score", TextView.class)
          .text("%d points", turn.score());

      view.find("timer", TextView.class)
          .text("%s %d:%02d",
              turn.timer().running() ? "" : "[paused]",
              turn.timer().remainingMinutes(),
              turn.timer().remainingSeconds());

    //  view.find("words", TableView.class)
    //      .table(fromMap(turn.words().asMap()));
    });

    //view.find("input", TextView.class)
    //    .text(":");
    //
    //view.setKeyPressListener(keyPress -> {
    //  if (keyPress.isSpaceBar()) {
    //    screen.eventHandler.resumeTimer();
    //  }
    //});
  }
}
