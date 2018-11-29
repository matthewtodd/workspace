package org.matthewtodd.console;

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
