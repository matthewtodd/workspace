package org.matthewtodd.console;

public interface Device {
  int rows();

  int columns();

  void replace(int row, int column, String content);

  void clear();

  default Size measuredWidth() {
    return Size.exactly(columns());
  }

  default Size measuredHeight() {
    return Size.exactly(rows());
  }

  default Rect rect() {
    return Rect.sized(rows(), columns());
  }

  default Canvas canvas() {
    return new Canvas(this, rect());
  }
}
