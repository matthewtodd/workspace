package org.matthewtodd.console;

public class HorizontalRule extends View {
  @Override protected void onDraw(Canvas canvas) {
    canvas.fill('-');
  }

  @Override protected int height() {
    return 1;
  }
}
