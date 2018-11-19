package org.matthewtodd.perquackey.console;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.matthewtodd.console.Device;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

class StringDevice implements Device, Iterable<String> {
  private final int rows;
  private final int columns;
  private final StringBuilder display = new StringBuilder();

  static Builder rows(int rows) {
    return new Builder(rows);
  }

  static class Builder {
    private int rows;
    private int columns;

    private Builder(int rows) {
      this.rows = rows;
    }

    Builder columns(int columns) {
      this.columns = columns;
      return this;
    }

    StringDevice build() {
      StringDevice device = new StringDevice(rows, columns);
      device.clear();
      return device;
    }
  }

  private StringDevice(int rows, int columns) {
    this.rows = rows;
    this.columns = columns;
  }

  @Override public int rows() {
    return rows;
  }

  @Override public int columns() {
    return columns;
  }

  @Override public void replace(int row, int column, String content) {
    int start = (row - 1) * (columns() + 1) + column - 1;
    int end = start + content.length();
    display.replace(start, end, content);
  }

  @Override public void clear() {
    display.setLength(0);
    display.append(join("\n", nCopies(rows(), join("", nCopies(columns(), " ")))));
  }

  @Override public String toString() {
    return display.toString();
  }

  @Override public Iterator<String> iterator() {
    return displayedRows().iterator();
  }

  private List<String> displayedRows() {
    return Arrays.asList(toString().split("\n"));
  }
}
