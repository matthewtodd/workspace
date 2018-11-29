package org.matthewtodd.console;

import java.util.Objects;

public class TextView extends SingleView {
  private String text;

  public TextView(String id) {
    super(id);
  }

  public void text(String newText) {
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
    setMeasuredDimensions(text.length(), 1);
  }

  @Override void onDraw(Canvas canvas) {
    canvas.text(text);
  }
}
