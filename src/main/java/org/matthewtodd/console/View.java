package org.matthewtodd.console;

import org.reactivestreams.Publisher;

public class View {
  private final Coordinator coordinator;
  private Window window;

  public View(Coordinator coordinator) {
    this.coordinator = coordinator;
  }

  void attach(Window window) {
    this.window = window;
    coordinator.attach(this);
  }

  void detach() {
    coordinator.detach(this);
    this.window = null;
  }

  protected void printf(String format, Object ... args) {
    window.output.printf(format, args);
  }

  public Publisher<String> input() {
    return window.input;
  }
}
