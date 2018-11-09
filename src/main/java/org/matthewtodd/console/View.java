package org.matthewtodd.console;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.matthewtodd.console.Window.KeyPress;

public abstract class View<SELF extends View> {
  static final View EMPTY = new View() {
    @Override protected void onDraw(Canvas canvas) { }
  };

  private final AtomicReference<Consumer<SELF>> attachmentListener;
  private final AtomicReference<Runnable> detachmentListener;
  private final AtomicReference<Runnable> invalidationListener;
  private final AtomicReference<Consumer<KeyPress>> keyPressListener;

  public View() {
    attachmentListener = new AtomicReference<>(t -> { });
    detachmentListener = new AtomicReference<>(() -> { });
    invalidationListener = new AtomicReference<>(() -> { });
    keyPressListener = new AtomicReference<>(t -> { });
  }

  public final SELF setAttachmentListener(Consumer<SELF> listener) {
    attachmentListener.set(listener);
    return self();
  }

  public final SELF setDetachmentListener(Runnable listener) {
    detachmentListener.set(listener);
    return self();
  }

  final SELF setInvalidationListener(Runnable listener) {
    invalidationListener.set(listener);
    return self();
  }

  public final SELF setKeyPressListener(Consumer<KeyPress> listener) {
    keyPressListener.set(listener);
    return self();
  }

  final SELF attachedToWindow() {
    attachmentListener.get().accept(self());
    return self();
  }

  final SELF detachedFromWindow() {
    detachmentListener.get().run();
    return self();
  }

  final SELF draw(Canvas canvas) {
    onDraw(canvas);
    return self();
  }

  final SELF keyPress(KeyPress keyPress) {
    keyPressListener.get().accept(keyPress);
    return self();
  }

  protected abstract void onDraw(Canvas canvas);

  protected final void invalidate() {
    invalidationListener.get().run();
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }
}
