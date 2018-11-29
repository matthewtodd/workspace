package org.matthewtodd.console;

import java.util.function.BiConsumer;

public interface Layout {
  // Maybe iterating over the children isn't what we want to do. Maybe we want find(id), since we'll have specs?
  // But I'm not sure how that'll work for a table. So maybe build that at the same time.
  // Let's start with dependency-ordering the constraint layout constraints.
  void measure(Size width, Size height, Iterable<View> children, BiConsumer<Integer, Integer> measuredParentDimensions);

  void layout(Rect bounds, Iterable<View> children);
}
