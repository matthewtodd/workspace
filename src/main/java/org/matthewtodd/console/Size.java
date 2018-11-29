package org.matthewtodd.console;

final class Size {
  static Size atMost(int limit) {
    return new Size();
  }

  static Size exactly(int size) {
    return new Size();
  }

  static Size unspecified() {
    return new Size();
  }

  int full() {
    return 0;
  }
}
