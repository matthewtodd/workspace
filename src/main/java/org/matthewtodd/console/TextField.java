package org.matthewtodd.console;

public class TextField extends TextView {
  private final StringBuilder buffer;

  public TextField(String id) {
    super(id);
    buffer = new StringBuilder();
    update();
  }

  public void append(String contents) {
    buffer.append(contents);
    update();
  }

  public void backspace() {
    buffer.setLength(Math.max(buffer.length() - 1, 0));
    update();
  }

  public void clear() {
    buffer.setLength(0);
    update();
  }

  public String value() {
    return buffer.toString();
  }

  private void update() {
    text(":%s", buffer);
  }
}
