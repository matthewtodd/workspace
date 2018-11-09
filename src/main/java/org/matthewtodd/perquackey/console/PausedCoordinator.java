package org.matthewtodd.perquackey.console;

import org.matthewtodd.console.Coordinator;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.PausedScreen;

class PausedCoordinator implements Coordinator<TurnView> {
  private final PausedScreen screen;

  PausedCoordinator(PausedScreen screen) {
    this.screen = screen;
  }

  @Override public void attach(TurnView view) {
    Flow.of(screen.screenData).subscribe(view::data);

    view.setKeyPressListener(keyPress -> {
      if (keyPress.isSpaceBar()) {
        screen.eventHandler.resumeTimer();
      }
    });
  }
}
