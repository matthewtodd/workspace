package org.matthewtodd.console;

import java.util.Collection;

public interface ViewContext {
  ViewContext NONE = new ViewContext() {
    @Override public void invalidateLayout() { }
    @Override public void dirty(Collection<Rect> dirty) { }
    @Override public void invalidate(View view) { }
  };

  void invalidateLayout();

  void dirty(Collection<Rect> dirty);

  void invalidate(View view);
}
