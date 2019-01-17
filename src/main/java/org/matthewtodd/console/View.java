package org.matthewtodd.console;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class View {
  static final View EMPTY = new SingleView("EMPTY") {
    @Override void onMeasure(Size width, Size height) { }
    @Override void onDraw(Canvas canvas) { }
  };

  public final String id;

  private ViewContext context = ViewContext.NONE;
  private int measuredWidth; // initialize to 0 or something
  private int measuredHeight;
  private Rect bounds = Rect.sized(0, 0);

  private Consumer<View> attached = v -> {};
  private Consumer<View> detached = v -> {};
  private Consumer<KeyPress> keyPress = k -> {};

  View(String id) {
    this.id = id;
  }

  // TODO maybe there's some way events propagate down the heirarchy.
  // Also, tabbing to focus.
  // Not going to think about that yet.
  final void attached(ViewContext viewContext) {
    context = viewContext;
    onAttached(viewContext);
    attached.accept(this);
  }

  abstract void onAttached(ViewContext context);

  final void detached() {
    context = ViewContext.NONE;
    onDetached();
    detached.accept(this);
  }

  abstract void onDetached();

  public final View attachmentListeners(Consumer<View> attached, Consumer<View> detached) {
    this.attached = attached;
    this.detached = detached;
    return this;
  }

  final void keyPress(KeyPress e) {
    keyPress.accept(e);
  }

  public final void keyPressListener(Consumer<KeyPress> listener) {
    keyPress = listener;
  }

  final void invalidateLayout() {
    context.invalidateLayout();
  }

  final void measure(Size width, Size height) {
    setMeasuredDimensions(Integer.MIN_VALUE, Integer.MIN_VALUE);
    onMeasure(width, height);
    if (measuredWidth == Integer.MIN_VALUE || measuredHeight == Integer.MIN_VALUE) {
      throw new IllegalStateException(String.format("Failed to measure View %s.", id));
    }
  }

  abstract void onMeasure(Size width, Size height);

  final void setMeasuredDimensions(int width, int height) {
    measuredWidth = width;
    measuredHeight = height;
  }

  final int getMeasuredWidth() {
    return measuredWidth;
  }

  final int getMeasuredHeight() {
    return measuredHeight;
  }

  final void layout(Rect newBounds) {
    context.dirty(bounds.diff(newBounds));
    bounds = newBounds;
    onLayout(newBounds);
  }

  abstract void onLayout(Rect bounds);

  final void invalidate() {
    context.invalidate(this);
  }

  final void draw(Canvas canvas) {
    onDraw(canvas.bounds(bounds));
  }

  abstract void onDraw(Canvas canvas);

  public abstract <T> T find(String id, Class<T> viewClass);

  @Override public boolean equals(Object other) {
    return other instanceof View && Objects.equals(id, ((View) other).id);
  }

  @Override public int hashCode() {
    return id.hashCode();
  }
}
