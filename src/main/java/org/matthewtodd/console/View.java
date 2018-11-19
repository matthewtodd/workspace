package org.matthewtodd.console;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class View {
  static final View EMPTY = new View() {
    @Override public void onDraw(Canvas canvas) { }
  };

  private final String id;
  private boolean invalidated = false;
  private Consumer<View> attachmentListener = t -> { };
  private Runnable detachmentListener = () -> { };
  private Runnable invalidationListener = () -> { };
  private Consumer<KeyPress> keyPressListener = t -> { };

  View() {
    this(UUID.randomUUID().toString());
  }

  View(String id) {
    this.id = id;
  }

  public final void setAttachmentListener(Consumer<View> listener) {
    attachmentListener = listener;
  }

  public final void setDetachmentListener(Runnable listener) {
    detachmentListener = listener;
  }

  final void setInvalidationListener(Runnable listener) {
    invalidationListener = listener;
  }

  public final void setKeyPressListener(Consumer<KeyPress> listener) {
    keyPressListener = listener;
  }

  final String id() {
    return id;
  }

  final void attachedToWindow() {
    attachmentListener.accept(this);
  }

  final void detachedFromWindow() {
    detachmentListener.run();
  }

  final void draw(Canvas canvas) {
    if (invalidated) {
      onDraw(canvas);
      invalidated = false;
    }
  }

  final void keyPress(KeyPress keyPress) {
    keyPressListener.accept(keyPress);
  }

  final void invalidate() {
    invalidated = true;
    invalidationListener.run();
  }

  final void layout(Rect rect) {
    invalidated = true; // not invalidate so we don't n^2 bounce all over the tree.
    onLayout(rect);
  }

  public <T> T find(String id, Class<T> viewClass) {
    return id().equals(id) ? viewClass.cast(this) : null;
  }

  protected abstract void onDraw(Canvas canvas);

  protected void onLayout(Rect rect) { }

  int height() {
    return Integer.MAX_VALUE;
  }

  int width() {
    return Integer.MAX_VALUE;
  }
}
