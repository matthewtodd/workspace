package org.matthewtodd.perquackey.console;

import org.matthewtodd.console.TextView;
import org.matthewtodd.console.View;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.perquackey.SpellingScreen;

class SpellingCoordinator implements Coordinator {
  private final SpellingScreen screen;
  private final StringBuilder buffer;

  SpellingCoordinator(SpellingScreen screen) {
    this.screen = screen;
    this.buffer = new StringBuilder(); // TODO make a TextField and push this inside!
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
    });

    view.setKeyPressListener(keyPress -> {
      if (keyPress.isSpaceBar()) {
        screen.eventHandler.pauseTimer();
      } else if (keyPress.isLowerCaseLetter()) {
        buffer.append(keyPress.stringValue());
        view.find("input", TextView.class).text(":%s", buffer.toString());
      } else if (keyPress.isBackspace()) {
        buffer.setLength(Math.max(buffer.length() - 1, 0));
        view.find("input", TextView.class).text(":%s", buffer.toString());
      } else if (keyPress.isEnter()) {
        screen.eventHandler.spell(buffer.toString());
        buffer.setLength(0);
        view.find("input", TextView.class).text(":%s", buffer.toString());
      } else {
        view.find("input", TextView.class).text(keyPress.toString());
      }
    });
  }
}
