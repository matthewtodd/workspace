package org.matthewtodd.console;

import java.util.Objects;

public abstract class SingleView extends View {
  SingleView(String id) {
    super(id);
  }

  @Override void onAttached(ViewContext context) {

  }

  @Override void onDetached() {

  }

  @Override void onLayout(Rect bounds) {

  }

  @Override public <T> T find(String id, Class<T> viewClass) {
    return Objects.equals(id, this.id) ? viewClass.cast(this) : null;
  }
}
