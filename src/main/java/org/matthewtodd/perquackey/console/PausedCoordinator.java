package org.matthewtodd.perquackey.console;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;
import org.matthewtodd.workflow.WorkflowScreen;

class PausedCoordinator implements Coordinator<Perquackey.TurnView> {
   private final PausedScreen screen;

   PausedCoordinator(WorkflowScreen<?, ?> screen) {
     this.screen = (PausedScreen) screen;
   }

  @Override public void attach(Perquackey.TurnView view) {
    view.input.setListener(new Perquackey.CommandLine.Listener() {
      @Override public void onSpace() {
        screen.eventHandler.resumeTimer();
      }
    });

    Flow.of(screen.screenData).subscribe(turn -> {
      view.score.setText(String.format("%d points", turn.score()));
      view.timer.setText(String.format("%s %d:%02d",
          turn.timer().running() ? "" : "[paused]",
          turn.timer().remainingMinutes(),
          turn.timer().remainingSeconds()));
    });

    view.input.takeFocus();
  }

  @Override public void detach(Perquackey.TurnView view) {
    view.input.setListener(null);
  }
}
