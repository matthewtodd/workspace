package org.matthewtodd.console;

import static java.lang.Integer.MAX_VALUE;

public class HorizontalRule extends SingleView {
  public HorizontalRule(String id) {
    super(id);
  }

  @Override void onMeasure(Size width, Size height) {
    setMeasuredDimensions(width.requesting(MAX_VALUE), height.requesting(MAX_VALUE));
  }

  @Override void onDraw(Canvas canvas) {
    canvas.fill('-');
  }
}
