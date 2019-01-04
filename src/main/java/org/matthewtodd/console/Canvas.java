package org.matthewtodd.console;

import java.util.Arrays;

class Canvas {
  private final Device device;
  private final Rect bounds;

  Canvas(Device device, Rect bounds) {
    this.device = device;
    this.bounds = bounds;
  }

  Canvas bounds(Rect bounds) {
    return new Canvas(device, bounds);
  }

  void clear() {
    // TODO push this into device. I think the terminal can do a better job.
    fill(' ');
  }

  void fill(char symbol) {
    for (int i = 0; i < bounds.height(); i++) {
      device.replace(bounds.top() + i, bounds.left(), repeat(symbol, bounds.width()));
    }
  }

  private static String repeat(char fill, int count) {
    char[] buffer = new char[count];
    Arrays.fill(buffer, fill);
    return new String(buffer);
  }

  void text(String text) {
    device.replace(bounds.top(), bounds.left(), text);
  }
}
