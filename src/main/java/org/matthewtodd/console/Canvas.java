package org.matthewtodd.console;

import java.util.Arrays;

public class Canvas {
  private final Device device;
  private final Rect rect;

  public static Canvas root(Device device) {
    return new Canvas(device, Rect.sized(device.rows(), device.columns()));
  }

  private Canvas(Device device, Rect rect) {
    this.device = device;
    this.rect = rect;
  }

  Canvas rect(Rect rect) {
    return new Canvas(device, rect);
  }

  void fill(char symbol) {
    for (int i = 0; i < rect.height(); i++) {
      device.replace(rect.top() + i, rect.left(), repeat(symbol, rect.width()));
    }
  }

  private String repeat(char fill, int count) {
    char[] buffer = new char[count];
    Arrays.fill(buffer, fill);
    return new String(buffer);
  }

  void text(String text, Alignment alignment) {
    device.replace(rect.top(), rect.left(), alignment.justify(text, rect.width()));
  }
}
