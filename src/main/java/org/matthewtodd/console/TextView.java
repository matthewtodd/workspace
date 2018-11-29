package org.matthewtodd.console;

import java.util.Objects;

public class TextView extends SingleView {
  private String text = "";

  public TextView(String id) {
    super(id);
  }

  public void text(String format, Object... args) {
    String newText = String.format(format, args);

    boolean needsLayout = newText.length() != text.length();
    boolean needsRedraw = !Objects.equals(newText, text);

    text = newText;

    if (needsRedraw) {
      if (needsLayout) {
        invalidateLayout();
      }
      invalidate();
    }
  }

  @Override void onMeasure(Size width, Size height) {
    setMeasuredDimensions(width.requesting(text.length()), height.requesting(1));
  }

  @Override void onDraw(Canvas canvas) {
    canvas.text(text);
  }
}
