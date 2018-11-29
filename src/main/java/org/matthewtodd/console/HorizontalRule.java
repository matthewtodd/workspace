package org.matthewtodd.console;

public class HorizontalRule extends SingleView {
  public HorizontalRule(String id) {
    super(id);
  }

  @Override void onMeasure(Size width, Size height) {
    setMeasuredDimensions(width.full(), height.full());
  }

  @Override void onDraw(Canvas canvas) {
    canvas.fill('-');
  }
}
