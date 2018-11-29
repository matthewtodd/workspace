package org.matthewtodd.console;

import java.util.function.BiConsumer;

public class ConstraintLayout implements Layout {
  public static ConstraintBuilder constrain(String id) {
    return new ConstraintBuilder(id);
  }

  public static class ConstraintBuilder {
    private final String id;

    ConstraintBuilder(String id) {
      this.id = id;
    }

    public VerticalConstraintBuilder top() {
      return new VerticalConstraintBuilder(id, "top");
    }

    public HorizontalConstraintBuilder left() {
      return new HorizontalConstraintBuilder(id, "left");
    }

    public DimensionConstraintBuilder width() {
      return new DimensionConstraintBuilder(id, "width");
    }

    public DimensionConstraintBuilder height() {
      return new DimensionConstraintBuilder(id, "height");
    }

    public HorizontalConstraintBuilder right() {
      return new HorizontalConstraintBuilder(id, "right");
    }

    public VerticalConstraintBuilder bottom() {
      return new VerticalConstraintBuilder(id, "bottom");
    }
  }

  public static class VerticalConstraintBuilder {
    private final String id;
    private final String property;

    private VerticalConstraintBuilder(String id, String property) {
      this.id = id;
      this.property = property; // TODO enum this?
    }

    public Constraint toTopOfParent() {
      return new Constraint();
    }

    public Constraint toBottomOf(String id) {
      return new Constraint();
    }

    public Constraint toTopOf(String id) {
      return new Constraint();
    }

    public Constraint toBottomOfParent() {
      return new Constraint();
    }
  }

  public static class HorizontalConstraintBuilder {
    private final String id;
    private final String property;

    private HorizontalConstraintBuilder(String id, String property) {
      this.id = id;
      this.property = property;
    }

    public Constraint toLeftOfParent() {
      return new Constraint();
    }

    public Constraint toRightOfParent() {
      return new Constraint();
    }
  }

  public static class DimensionConstraintBuilder {
    private final String id;
    private final String property;

    private DimensionConstraintBuilder(String id, String property) {
      this.id = id;
      this.property = property;
    }

    public Constraint selfDetermined() {
      return new Constraint();
    }

    public Constraint fixed(int i) {
      return new Constraint();
    }

    public Constraint toMatchParent() {
      return new Constraint();
    }
  }

  public static class Constraint {

  }

  public ConstraintLayout(Constraint... constraints) {

  }

  @Override public void measure(Size width, Size height, Iterable<View> children,
      BiConsumer<Integer, Integer> measuredParentDimensions) {

  }

  @Override public void layout(Rect bounds, Iterable<View> children) {

  }
}
