package org.matthewtodd.console;

import java.util.concurrent.atomic.AtomicReference;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Publisher;

public class Window {
  private final AtomicReference<View> rootView = new AtomicReference<>(View.EMPTY);
  private final Device device;

  public Window(Publisher<Integer> input, Device device) {
    Flow.of(input).as(KeyPress::new).subscribe(k -> rootView.get().keyPress(k));
    // TODO listen to WINCH from device, then redraw (= clear and draw)
    this.device = device;
  }

  public void rootView(View view) {
    rootView.get()
        .setInvalidationListener(() -> {})
        .detachedFromWindow();
    device.clear();
    rootView.set(view);
    rootView.get()
        .setInvalidationListener(this::draw)
        .attachedToWindow();
  }

  public void close() {
    rootView(View.EMPTY);
  }

  private void draw() {
    rootView.get().draw(Canvas.root(device));
  }

  public static class KeyPress {
    private final int keyCode;

    KeyPress(int keyCode) {
      this.keyCode = keyCode;
    }

    public boolean isBackspace() {
      return keyCode == 127;
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
}
