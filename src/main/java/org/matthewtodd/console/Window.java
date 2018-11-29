package org.matthewtodd.console;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Publisher;

public class Window {
  private final Consumer<Runnable> scheduler;
  private final Device device;
  private final Set<View> needsLayout = new LinkedHashSet<>();
  private final Set<Rect> needsClearing = new LinkedHashSet<>();
  private final Set<View> needsDrawing = new LinkedHashSet<>();
  private View rootView = View.EMPTY;

  public Window(Publisher<Integer> input, Consumer<Runnable> scheduler, Device device) {
    Flow.of(input)
        .as(KeyPress::new)
        .subscribe(e -> rootView.keyPress(e));

    this.scheduler = scheduler;
    this.device = device;
  }

  public void rootView(View view) {
    rootView.detached();
    rootView = view;

    rootView.attached(new ViewContext() {
      @Override public void invalidateLayout() {
        needsLayout.add(rootView);
        scheduler.accept(Window.this::eventLoop);
      }

      @Override public void dirty(Collection<Rect> dirty) {
        needsClearing.addAll(dirty);
        scheduler.accept(Window.this::eventLoop);
      }

      @Override public void invalidate(View view) {
        needsDrawing.add(view);
        scheduler.accept(Window.this::eventLoop);
      }
    });
  }

  public void close() {
    rootView.detached();
    device.clear();
  }

  private void eventLoop() {
    drain(needsLayout, rootView -> {
      // rootView.accept()
      rootView.measure(Size.exactly(device.columns()), Size.exactly(device.rows()));
      rootView.layout(Rect.sized(device.rows(), device.columns()));
    });

    drain(needsClearing, dirty ->
        Canvas.root(device).bounds(dirty).clear());

    drain(needsDrawing, view ->
        view.draw(Canvas.root(device)));
  }

  private static <T> void drain(Set<T> set, Consumer<T> action) {
    set.forEach(action);
    set.clear();
  }
}
