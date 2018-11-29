package org.matthewtodd.console;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class View {
  static final View EMPTY = new View("EMPTY") {
    @Override void onAttached(ViewContext context) { }
    @Override void onDetached() { }
    @Override void onMeasure(Size width, Size height) { }
    @Override void onLayout(Rect bounds) { }
    @Override void onDraw(Canvas canvas) { }
  };

  private final String id;

  private ViewContext context = ViewContext.NONE;
  private int measuredWidth; // initialize to 0 or something
  private int measuredHeight;
  private Rect bounds;

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
    onMeasure(width, height);
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

  @Override public boolean equals(Object other) {
    return other instanceof View && Objects.equals(id, ((View) other).id);
  }

  @Override public int hashCode() {
    return id.hashCode();
  }
}
