package org.matthewtodd.console;

import java.util.function.Consumer;
import java.util.function.Function;

public class Canvas {
  private final Device device;
  private final int originRow;
  private final int originColumn;
  private final int rows;
  private final int columns;

  static Canvas root(Device device) {
    return new Canvas(device, 1, 1, device.rows(), device.columns());
  }

  private Canvas(Device device, int originRow, int originColumn, int rows, int columns) {
    this.device = device;
    this.originRow = originRow;
    this.originColumn = originColumn;
    this.rows = rows;
    this.columns = columns;
  }

  public Canvas area(Consumer<BoundedCanvasBuilder> constraints) {
    BoundedCanvasBuilder builder = new BoundedCanvasBuilder();
    constraints.accept(builder);
    return builder.build(device);
  }

  public Text leftAligned() {
    return new Text(this::pad);
  }

  public Text rightAligned() {
    return new Text(s -> reverse(pad(reverse(s))));
  }

  public void rule() {
    device.replace(originRow, originColumn, pad("").replace(' ', '-'));
  }

  private String pad(String s) {
    return String.format("%-" + columns + "s", s).substring(0, columns);
  }

  private String reverse(String s) {
    return new StringBuilder(s).reverse().toString();
  }

  public class BoundedCanvasBuilder {
    private int newOriginRow = originRow;
    private int newOriginColumn = originColumn;
    private int newRows = 1;
    private int newColumns = columns;

    public BoundedCanvasBuilder top() {
      return top(0);
    }

    public BoundedCanvasBuilder top(int offset) {
      newOriginRow = originRow + offset;
      newRows = 1;
      return this;
    }

    public BoundedCanvasBuilder bottom() {
      return bottom(0);
    }

    public BoundedCanvasBuilder bottom(int offset) {
      newOriginRow = originRow + rows - 1 - offset;
      newRows = 1;
      return this;
    }

    public BoundedCanvasBuilder left(int originColumn) {
      newOriginColumn = originColumn;
      return this;
    }

    public BoundedCanvasBuilder leftHalf() {
      newOriginColumn = originColumn;
      newColumns = columns / 2;
      return this;
    }

    public BoundedCanvasBuilder rightHalf() {
      newOriginColumn = columns - (columns / 2) + 1;
      newColumns = columns / 2;
      return this;
    }

    public BoundedCanvasBuilder width(int columns) {
      newColumns = columns;
      return this;
    }

    private Canvas build(Device device) {
      return new Canvas(device, newOriginRow, newOriginColumn, newRows, newColumns);
    }
  }

  public class Text {
    private final Function<String, String> justifier;

    private Text(Function<String, String> justifier) {
      this.justifier = justifier;
    }

    public void text(String format, Object... args) {
      device.replace(originRow, originColumn, justifier.apply(String.format(format, args)));
    }
  }
}
