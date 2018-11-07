package org.matthewtodd.console;

public interface Coordinator<V extends View> {
  void attach(V view);

  void detach();
}
