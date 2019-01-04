package org.matthewtodd.console;

import java.util.function.Function;

public interface Layout {
  void measure(Size width, Size height, Function<String, View> finder);

  void layout(Rect bounds, Function<String, View> finder);
}
