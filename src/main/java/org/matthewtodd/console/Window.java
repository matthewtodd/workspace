package org.matthewtodd.console;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Publisher;

public class Window {
  private final AtomicReference<View> rootView = new AtomicReference<>(View.EMPTY);
  private final Size size;
  private final Canvas canvas;

  public Window(Publisher<KeyPress> input, int rows, int columns, Consumer<Stroke> output) {
    Flow.of(input).subscribe(rootView.get()::keyPress);
    size = new Size(rows, columns);
    canvas = new Canvas(output);
  }

  public void rootView(View view) {
    rootView.get()
        .setInvalidationListener(() -> {})
        .detachedFromWindow();
    rootView.set(view);
    rootView.get()
        .setInvalidationListener(this::draw)
        .attachedToWindow();
  }

  private void draw() {
    rootView.get()
        .layout(0, 0, size.columns, size.rows)
        .draw(canvas);
  }

  private static class Size {
    private final int rows;
    private final int columns;

    Size(int rows, int columns) {
      this.rows = rows;
      this.columns = columns;
    }
  }

  public static class KeyPress {
    private final int keyCode;

    public KeyPress(int keyCode) {
      this.keyCode = keyCode;
    }

    public boolean isEnter() {
      return keyCode == 13;
    }

    public boolean isLowerCaseLetter() {
      return Character.isLowerCase(keyCode);
    }

    public boolean isSpaceBar() {
      return keyCode == 32;
    }

    public String stringValue() {
      if (!isLowerCaseLetter()) {
        throw new IllegalStateException(String.format("Not a lowercase letter: %s", this));
      }
      return String.valueOf(Character.toChars(keyCode));
    }

    @Override public String toString() {
      return "KeyPress{" +
          "keyCode=" + keyCode +
          '}';
    }
  }

  public static class Stroke {

  }
}
