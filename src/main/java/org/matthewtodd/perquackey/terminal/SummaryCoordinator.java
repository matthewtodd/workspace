package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.input.KeyType;
import org.matthewtodd.perquackey.SummaryScreen;
import org.matthewtodd.terminal.Coordinator;

public class SummaryCoordinator implements Coordinator<SummaryView> {
  private final SummaryScreen screen;

  SummaryCoordinator(SummaryScreen screen) {
    this.screen = screen;
  }

  @Override public void attach(SummaryView view) {
    view.commandLine.setKeyPressListener(keyStroke -> {
      if (keyStroke.getKeyType() == KeyType.Character) {
        if (keyStroke.getCharacter() == 'Q') {
          screen.eventHandler.quit();
        }
      }
    });

    view.commandLine.takeFocus();
  }
}
