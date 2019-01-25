package org.matthewtodd.perquackey.console;

// import org.matthewtodd.console.TextField;
// import org.matthewtodd.console.TextView;
// import org.matthewtodd.console.View;
// import org.matthewtodd.flow.Flow;
// import org.matthewtodd.perquackey.SpellingScreen;
// import org.matthewtodd.workflow.WorkflowScreen;

class SpellingCoordinator implements Coordinator {
  // private final SpellingScreen screen;

  // SpellingCoordinator(WorkflowScreen<?, ?> screen) {
  //   this.screen = (SpellingScreen) screen;
  // }

  // @Override public void attach(View view) {
  //   Flow.of(screen.screenData).subscribe(turn -> {
  //     view.find("score", TextView.class)
  //         .text("%d points", turn.score());

  //     view.find("timer", TextView.class)
  //         .text("%s %d:%02d",
  //             turn.timer().running() ? "" : "[paused]",
  //             turn.timer().remainingMinutes(),
  //             turn.timer().remainingSeconds());
  //   });

  //   view.keyPressListener(keyPress -> {
  //     if (keyPress.isSpaceBar()) {
  //       screen.eventHandler.pauseTimer();
  //     } else {
  //       TextField input = view.find("input", TextField.class);

  //       if (keyPress.isLowerCaseLetter()) {
  //         input.append(keyPress.stringValue());
  //       } else if (keyPress.isBackspace()) {
  //         input.backspace();
  //       } else if (keyPress.isEnter()) {
  //         screen.eventHandler.spell(input.value());
  //         input.clear();
  //       }
  //     }
  //   });
  // }
}
