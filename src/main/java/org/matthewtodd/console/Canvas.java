package org.matthewtodd.console;

import java.util.function.Consumer;
import org.matthewtodd.console.Window.Stroke;

public class Canvas {
  private final Consumer<Stroke> output;

  Canvas(Consumer<Stroke> output) {
    this.output = output;
  }
}
