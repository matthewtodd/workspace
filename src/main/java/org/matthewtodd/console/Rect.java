package org.matthewtodd.console;

import java.util.Collection;
import java.util.Collections;

class Rect {
  private final int top;
  private final int right; // exclusive
  private final int bottom; // exclusive
  private final int left;

  static Rect sized(int rows, int columns) {
    return new Rect(1, columns + 1, rows + 1, 1);
  }

  private Rect(int top, int right, int bottom, int left) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }

  int top() {
    return top;
  }

  int bottom() {
    return bottom;
  }

  int height() {
    return bottom - top;
  }

  Rect rows(int top, int height) {
    return new Rect(top, right, top + height, left);
  }

  int left() {
    return left;
  }

  int width() {
    return right - left;
  }

  Rect columns(int left, int width) {
    return new Rect(top, left + width, bottom, left);
  }

  @Override public String toString() {
    return String.format("[%d, %d, %d, %d]", top, right, bottom, left);
  }

  Collection<Rect> diff(Rect newBounds) {
    // TODO implement this!
    return Collections.emptyList();
  }
}
