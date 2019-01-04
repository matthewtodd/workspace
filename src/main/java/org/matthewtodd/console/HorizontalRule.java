package org.matthewtodd.console;

public class HorizontalRule extends SingleView {
  private final char symbol;

  public HorizontalRule(String id, char symbol) {
    super(id);
    this.symbol = symbol;
  }

  @Override void onMeasure(Size width, Size height) {
    setMeasuredDimensions(width.available(), height.available());
  }

  @Override void onDraw(Canvas canvas) {
    canvas.fill(symbol);
  }
}
