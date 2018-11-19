package org.matthewtodd.console;

public class KeyPress {
  private final int keyCode;

  KeyPress(int keyCode) {
    this.keyCode = keyCode;
  }

  public boolean isBackspace() {
    return keyCode == 127;
  }

  public boolean isEnter() {
    return keyCode == 13;
  }

  public boolean isLowerCaseLetter() {
    return Character.isLowerCase(keyCode);
  }

  public boolean isSpaceBar() {
    return keyCode == 32;
  }

  public String stringValue() {
    if (!isLowerCaseLetter()) {
      throw new IllegalStateException(String.format("Not a lowercase letter: %s", this));
    }
    return String.valueOf(Character.toChars(keyCode));
  }

  @Override public String toString() {
    return "KeyPress{" +
        "keyCode=" + keyCode +
        '}';
  }
}
