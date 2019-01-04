package org.matthewtodd.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class ConstraintLayout implements Layout {
  private final List<ViewConstraints> viewConstraints;
  private final Map<String, Rect> clips;

  public ConstraintLayout(Constraint... constraints) {
    viewConstraints = new ConstrainerGraph(constraints).toUnmodifiableList();
    clips = new LinkedHashMap<>();
  }

  @Override public void measure(Size width, Size height, Function<String, View> finder) {
    clips.clear();

    viewConstraints.forEach(c -> {
      View view = finder.apply(c.id);
      c.measure(view, width, height, clips);
    });
  }

  @Override public void layout(Rect bounds, Function<String, View> finder) {
    viewConstraints.forEach(c -> {
      View view = finder.apply(c.id);
      Rect clip = clips.get(c.id);
      Objects.requireNonNull(clip, String.format("Could not find clip for View %s", c.id));
      view.layout(bounds.clip(clip));
    });
  }

  // If we can still get by with just topologically sorting the viewConstraints,
  // then this sorting mechanism could be made constrainer-agnostic.
  // We just need a key extractor and a dependencies extractor, I suppose.
  //  (Which I guess would be nicer than some "TopologicallySortable" interface?)
  private static class ConstrainerGraph {
    private final Map<String, ViewConstraints> constrainers = new LinkedHashMap<>();
    private final Map<String, Integer> weights = new LinkedHashMap<>();

    // TODO push constrainer-building out of here.
    // We have enough responsibilities with the topological sorting.
    ConstrainerGraph(Constraint[] constraints) {
      Arrays.stream(constraints)
          .collect(groupingBy(Constraint::id))
          .forEach(this::add);
    }

    private void add(String id, List<Constraint> constraints) {
      constrainers.put(id, new ViewConstraints(id, constraints));
      ensureWeight(id, 0);
    }

    private void ensureWeight(String id, int weight) {
      if (!weights.containsKey(id) || weights.get(id) < weight) {
        weights.put(id, weight);
      }

      if (constrainers.containsKey(id)) {
        constrainers.get(id).forEachDependency(dep -> ensureWeight(dep, weight + 1));
      }
    }

    List<ViewConstraints> toUnmodifiableList() {
      List<ViewConstraints> result = new ArrayList<>(constrainers.values());
      result.sort(comparing(this::weight).reversed());
      return Collections.unmodifiableList(result);
    }

    private int weight(ViewConstraints c) {
      return weights.get(c.id);
    }
  }

  private static class ViewConstraints {
    final String id;
    final List<Constraint> constraints;

    ViewConstraints(String id, List<Constraint> constraints) {
      this.id = id;
      this.constraints = constraints;
    }

    void forEachDependency(Consumer<String> action) {
      constraints.stream()
          .map(Constraint::dependencyId)
          .filter(Objects::nonNull)
          .collect(toSet())
          .forEach(action);
    }

    void measure(View view, Size parentWidth, Size parentHeight, Map<String, Rect> insets) {
      Rect.Builder builder = new Rect.Builder();

      for (Constraint constraint : constraints) {
        constraint.resolveFixed(builder);
      }

      for (Constraint constraint : constraints) {
        constraint.resolveRelativeToParent(builder, parentWidth, parentHeight);
      }

      for (Constraint constraint : constraints) {
        constraint.resolveRelativeToOther(builder, insets);
      }

      for (Constraint constraint : constraints) {
        constraint.resolveViewDetermined(builder, view, parentWidth, parentHeight);
      }

      Rect clip = builder.build();
      view.measure(Size.exactly(clip.width()), Size.exactly(clip.height()));
      insets.put(id, clip);
    }
  }

  public static class Constraint {
    private final String id;
    private final Property property;
    private final Source source;

    Constraint(String id, Property property, Source source) {
      this.id = id;
      this.property = property;
      this.source = source;
    }

    public static Builder constrain(String id) {
      return new Builder(id);
    }

    String id() {
      return id;
    }

    String dependencyId() {
      return source.dependencyId();
    }

    void resolveFixed(Rect.Builder builder) {
      if (source.isFixed()) {
        property.apply(builder, source.get());
      }
    }

    void resolveRelativeToParent(Rect.Builder builder, Size parentWidth, Size parentHeight) {
      if (source.isRelativeToParent()) {
        property.apply(builder, source.get(parentWidth, parentHeight));
      }
    }

    void resolveRelativeToOther(Rect.Builder builder, Map<String, Rect> insets) {
      if (source.isRelativeToOther()) {
        property.apply(builder, source.get(insets));
      }
    }

    void resolveViewDetermined(Rect.Builder builder, View view, Size parentWidth, Size parentHeight) {
      if (source.isViewDetermined()) {
        property.apply(builder, source.get(view, builder.clipWidth(parentWidth), builder.clipHeight(parentHeight)));
      }
    }

    private enum Property {
      LEFT {
        @Override void apply(Rect.Builder builder, int value) {
          builder.left(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return 1;
        }

        @Override int fromClip(Rect clip) {
          return clip.left();
        }
      },

      RIGHT {
        @Override void apply(Rect.Builder builder, int value) {
          builder.right(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return parentWidth + 1;
        }

        @Override int fromClip(Rect clip) {
          return clip.right();
        }
      },

      WIDTH {
        @Override void apply(Rect.Builder builder, int value) {
          builder.width(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return parentWidth;
        }

        @Override int fromClip(Rect clip) {
          return clip.width();
        }
      },

      TOP {
        @Override void apply(Rect.Builder builder, int value) {
          builder.top(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return 1;
        }

        @Override int fromClip(Rect clip) {
          return clip.top();
        }
      },

      BOTTOM {
        @Override void apply(Rect.Builder builder, int value) {
          builder.bottom(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return parentHeight + 1;
        }

        @Override int fromClip(Rect clip) {
          return clip.bottom();
        }
      },

      HEIGHT {
        @Override void apply(Rect.Builder builder, int value) {
          builder.height(value);
        }

        @Override int fromDimensions(int parentWidth, int parentHeight) {
          return parentHeight;
        }

        @Override int fromClip(Rect clip) {
          return clip.height();
        }
      };

      abstract void apply(Rect.Builder builder, int value);

      abstract int fromDimensions(int parentWidth, int parentHeight);

      abstract int fromClip(Rect clip);
    }

    private interface Source {
      static Source parent(Property property) {
        return new Source() {
          @Override public boolean isRelativeToParent() {
            return true;
          }

          @Override public int get(Size parentWidth, Size parentHeight) {
            return property.fromDimensions(parentWidth.available(), parentHeight.available());
          }
        };
      }

      static Source other(String id, Property property) {
        return new Source() {
          @Override public String dependencyId() {
            return id;
          }

          @Override public boolean isRelativeToOther() {
            return true;
          }

          @Override public int get(Map<String, Rect> clips) {
            return property.fromClip(clips.get(id));
          }
        };
      }

      static Source viewDetermined(Property property) {
        return new Source() {
          @Override public boolean isViewDetermined() {
            return true;
          }

          @Override public int get(View view, Size clippedWidth, Size clippedHeight) {
            view.measure(clippedWidth, clippedHeight);
            return property.fromDimensions(view.getMeasuredWidth(), view.getMeasuredHeight());
          }
        };
      }

      static Source fixed(int value) {
        return new Source() {
          @Override public boolean isFixed() {
            return true;
          }

          @Override public int get() {
            return value;
          }
        };
      }

      default String dependencyId() {
        return null;
      }

      default boolean isFixed() {
        return false;
      }

      default boolean isRelativeToParent() {
        return false;
      }

      default boolean isRelativeToOther() {
        return false;
      }

      default boolean isViewDetermined() {
        return false;
      }

      default int get() {
        return 0;
      }

      default int get(Size parentWidth, Size parentHeight) {
        return 0;
      }

      default int get(Map<String, Rect> insets) {
        return 0;
      }

      default int get(View view, Size clippingWidth, Size clippingHeight) {
        return 0;
      }
    }

    public static class Builder {
      private final String id;

      Builder(String id) {
        this.id = id;
      }

      public VerticalConstraintBuilder top() {
        return new VerticalConstraintBuilder(id, Property.TOP);
      }

      public HorizontalConstraintBuilder left() {
        return new HorizontalConstraintBuilder(id, Property.LEFT);
      }

      public DimensionConstraintBuilder width() {
        return new DimensionConstraintBuilder(id, Property.WIDTH);
      }

      public DimensionConstraintBuilder height() {
        return new DimensionConstraintBuilder(id, Property.HEIGHT);
      }

      public HorizontalConstraintBuilder right() {
        return new HorizontalConstraintBuilder(id, Property.RIGHT);
      }

      public VerticalConstraintBuilder bottom() {
        return new VerticalConstraintBuilder(id, Property.BOTTOM);
      }
    }

    public static class VerticalConstraintBuilder {
      private final String id;
      private final Property property;

      private VerticalConstraintBuilder(String id, Property property) {
        this.id = id;
        this.property = property;
      }

      public Constraint toTopOfParent() {
        return new Constraint(this.id, property, Source.parent(Property.TOP));
      }

      public Constraint toBottomOf(String id) {
        return new Constraint(this.id, property, Source.other(id, Property.BOTTOM));
      }

      public Constraint toTopOf(String id) {
        return new Constraint(this.id, property, Source.other(id, Property.TOP));
      }

      public Constraint toBottomOfParent() {
        return new Constraint(this.id, property, Source.parent(Property.BOTTOM));
      }
    }

    public static class HorizontalConstraintBuilder {
      private final String id;
      private final Property property;

      private HorizontalConstraintBuilder(String id, Property property) {
        this.id = id;
        this.property = property;
      }

      public Constraint toLeftOfParent() {
        return new Constraint(this.id, property, Source.parent(Property.LEFT));
      }

      public Constraint toRightOfParent() {
        return new Constraint(this.id, property, Source.parent(Property.RIGHT));
      }

      public Constraint toRightOf(String id) {
        return new Constraint(this.id, property, Source.other(id, Property.RIGHT));
      }
    }

    public static class DimensionConstraintBuilder {
      private final String id;
      private final Property property;

      private DimensionConstraintBuilder(String id, Property property) {
        this.id = id;
        this.property = property;
      }

      public Constraint selfDetermined() {
        return new Constraint(this.id, property, Source.viewDetermined(property));
      }

      public Constraint fixed(int value) {
        return new Constraint(this.id, property, Source.fixed(value));
      }
    }
  }
}
