package org.matthewtodd.console;

import java.util.Objects;

import static java.util.stream.StreamSupport.stream;

public abstract class ViewGroup extends View {
  private final Layout layout;

  ViewGroup(String id, Layout layout) {
    super(id);
    this.layout = layout;
  }

  abstract Iterable<View> children();

  @Override void onAttached(ViewContext context) {
    for (View child : children()) {
      child.attached(context);
    }
  }

  @Override void onDetached() {
    for (View child : children()) {
      child.detached();
    }
  }

  @Override public <T> T find(String id, Class<T> viewClass) {
    return Objects.equals(id, this.id)
        ? viewClass.cast(this)
        : stream(children().spliterator(), false)
            .map(c -> c.find(id, viewClass))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
  }

  @Override final void onMeasure(Size width, Size height) {
    layout.measure(width, height, children(), this::setMeasuredDimensions);
  }

  @Override final void onLayout(Rect bounds) {
    layout.layout(bounds, children());
  }

  @Override final void onDraw(Canvas canvas) {
    for (View child : children()) {
      child.draw(canvas);
    }
  }
}
