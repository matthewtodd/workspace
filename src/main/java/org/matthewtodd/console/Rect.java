package org.matthewtodd.console;

import java.util.Collection;
import java.util.Collections;

import static java.lang.Integer.MIN_VALUE;

class Rect {
  private final int top;
  private final int right; // exclusive
  private final int bottom; // exclusive
  private final int left;

  private Rect(int top, int right, int bottom, int left) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }

  static Rect sized(int rows, int columns) {
    return new Rect(1, columns + 1, rows + 1, 1);
  }

  int left() {
    return left;
  }

  int right() {
    return right;
  }

  int width() {
    return right - left;
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

  @Override public String toString() {
    return String.format("[%d, %d, %d, %d]", top, right, bottom, left);
  }

  Collection<Rect> diff(Rect newBounds) {
    // TODO implement this!
    return Collections.emptyList();
  }

  Rect clip(Rect clip) {
    // TODO assert relativeBounds fit inside this? Clip?
    int absoluteTop = top + clip.top - 1;
    int absoluteLeft = left + clip.left - 1;

    return new Rect(
        absoluteTop,
        absoluteLeft + clip.width(),
        absoluteTop + clip.height(),
        absoluteLeft);
  }

  static class Builder {
    private int left = MIN_VALUE;
    private int right = MIN_VALUE;
    private int width = MIN_VALUE;
    private int top = MIN_VALUE;
    private int bottom = MIN_VALUE;
    private int height = MIN_VALUE;

    Rect build() {
      return new Rect(
          top != MIN_VALUE ? top : bottom - height,
          right != MIN_VALUE ? right : left + width,
          bottom != MIN_VALUE ? bottom : top + height,
          left != MIN_VALUE ? left : right - width
      );
    }

    Size clipWidth(Size parentWidth) {
      if (width != MIN_VALUE) {
        return Size.atMost(width);
      } else {
        return parentWidth.trim(
            left != MIN_VALUE ? left : 1,
            right != MIN_VALUE ? right : parentWidth.available() + 1
        );
      }
    }

    Size clipHeight(Size parentHeight) {
      if (height != MIN_VALUE) {
        return Size.atMost(height);
      } else {
        return parentHeight.trim(
            top != MIN_VALUE ? top : 1,
            bottom != MIN_VALUE ? bottom : parentHeight.available() + 1
        );
      }
    }

    Builder left(int value) {
      left = value;
      return this;
    }

    Builder right(int value) {
      right = value;
      return this;
    }

    Builder width(int value) {
      width = value;
      return this;
    }

    Builder top(int value) {
      top = value;
      return this;
    }

    Builder bottom(int value) {
      bottom = value;
      return this;
    }

    Builder height(int value) {
      height = value;
      return this;
    }
  }
}
