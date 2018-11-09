package org.matthewtodd.perquackey.console;

import org.matthewtodd.console.Coordinator;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.SpellingScreen;

class SpellingCoordinator implements Coordinator<TurnView> {
  private final SpellingScreen screen;
  private final StringBuilder buffer;

  SpellingCoordinator(SpellingScreen screen) {
    this.screen = screen;
    this.buffer = new StringBuilder();
  }

  @Override public void attach(TurnView view) {
    Flow.of(screen.screenData).subscribe(view::data);

    view.setKeyPressListener(keyPress -> {
      if (keyPress.isSpaceBar()) {
        screen.eventHandler.pauseTimer();
      } else if (keyPress.isLowerCaseLetter()) {
        buffer.append(keyPress.stringValue());
        view.input(buffer.toString());
      } else if (keyPress.isBackspace()) {
        buffer.setLength(Math.max(buffer.length() - 1, 0));
        view.input(buffer.toString());
      } else if (keyPress.isEnter()) {
        screen.eventHandler.spell(buffer.toString());
        buffer.setLength(0);
        view.input(buffer.toString());
      } else {
        view.input(keyPress.toString());
      }
    });
  }
}
