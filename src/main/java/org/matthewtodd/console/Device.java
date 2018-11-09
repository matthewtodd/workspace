package org.matthewtodd.console;

public interface Device {
  int rows();

  int columns();

  void replace(int row, int column, String content);

  void clear();
}
