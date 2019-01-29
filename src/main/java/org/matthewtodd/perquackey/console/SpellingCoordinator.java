package org.matthewtodd.perquackey.console;

import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.SpellingScreen;
import org.matthewtodd.workflow.WorkflowScreen;

class SpellingCoordinator implements Coordinator<Perquackey.TurnView> {
   private final SpellingScreen screen;

   SpellingCoordinator(WorkflowScreen<?, ?> screen) {
     this.screen = (SpellingScreen) screen;
   }

  @Override public void attach(Perquackey.TurnView view) {
    view.input.setListener(new Perquackey.CommandLine.Listener() {
      @Override public void onSpace() {
        screen.eventHandler.pauseTimer();
      }

      @Override public void onEnter(String word) {
        screen.eventHandler.spell(word);
      }
    });

    Flow.of(screen.screenData).subscribe(turn -> {
      view.score.setText(String.format("%d points", turn.score()));
      view.timer.setText(String.format("%s%d:%02d",
          turn.timer().running() ? "" : "[paused] ",
          turn.timer().remainingMinutes(),
          turn.timer().remainingSeconds()));
    });

    view.input.takeFocus();
  }

  @Override public void detach(Perquackey.TurnView view) {
    view.input.setListener(null);
  }
}
