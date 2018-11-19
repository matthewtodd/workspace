package org.matthewtodd.console;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

public class TextView extends View {
  private final Alignment alignment;
  private final AtomicReference<String> text;

  public TextView(String id) {
    this(id, Alignment.LEFT);
  }

  public TextView(String id, Alignment alignment) {
    super(id);
    this.alignment = alignment;
    this.text = new AtomicReference<>("");
  }

  public void text(String format, Object... args) {
    text.set(format(format, args));
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    canvas.text(text.get(), alignment);
  }

  @Override int height() {
    return 1;
  }
}
