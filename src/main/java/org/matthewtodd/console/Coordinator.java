package org.matthewtodd.console;

public interface Coordinator {
  Coordinator NONE = new Coordinator() {
    @Override public void attach(View view) { }
    @Override public void detach(View view) { }
  };

  void attach(View view);

  void detach(View view);
}
