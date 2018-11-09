package org.matthewtodd.perquackey.console;

import java.util.concurrent.atomic.AtomicInteger;
import org.matthewtodd.console.Canvas;
import org.matthewtodd.console.View;
import org.matthewtodd.perquackey.Turn;

class TurnView extends View<TurnView> {
  private Turn.Snapshot turn;
  private String input = "";

  @Override protected void onDraw(Canvas canvas) {
    canvas.area(a -> a.top().leftHalf())
        .leftAligned()
        .text("%d points", turn.score());

    canvas.area(a -> a.top().rightHalf())
        .rightAligned()
        .text("%s %d:%02d",
            turn.timer().running() ? "": "[paused]",
            turn.timer().remainingMinutes(),
            turn.timer().remainingSeconds());

    canvas.area(a -> a.top(1))
        .rule();

    final AtomicInteger columnPosition = new AtomicInteger(1);
    turn.words().eachColumn(c -> {
      canvas.area(a -> a.top(2).left(columnPosition.get()).width(c.length()))
          .leftAligned()
          .text("%d", c.length());
      columnPosition.addAndGet(c.length());
      columnPosition.incrementAndGet();
    });

    canvas.area(a -> a.bottom(1))
        .rule();

    canvas.area(a -> a.bottom())
        .leftAligned()
        .text(":%s", input);
  }

  void data(Turn.Snapshot turn) {
    this.turn = turn;
    invalidate();
  }

  void input(String input) {
    this.input = input;
    invalidate();
  }
}
