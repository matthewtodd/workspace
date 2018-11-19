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
    rootView.get().setInvalidationListener(() -> { });
    rootView.get().detachedFromWindow();
    device.clear();
    rootView.set(view);
    rootView.get().setInvalidationListener(() -> rootView.get().draw(Canvas.root(device)));
    rootView.get().layout(Rect.sized(device.rows(), device.columns()));
    rootView.get().attachedToWindow();
  }

  public void close() {
    rootView(View.EMPTY);
  }
}
