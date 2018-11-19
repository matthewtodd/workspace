package org.matthewtodd.console;

public enum Alignment {
  LEFT("-"),
  RIGHT("");

  private final String flags;

  Alignment(String flags) {
    this.flags = flags;
  }

  String justify(String text, int width) {
    return String.format("%" + flags + width + "s", text);
  }
}
