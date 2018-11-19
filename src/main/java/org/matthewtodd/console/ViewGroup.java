package org.matthewtodd.console;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.collections4.sequence.CommandVisitor;
import org.apache.commons.collections4.sequence.SequencesComparator;

import static java.util.stream.Collectors.toList;

public class ViewGroup extends View {
  private final List<View> children;
  private final Orientation orientation;
  private final LinkedHashMap<String, Rect> rects;

  public enum Orientation {
    ROWS() {
      @Override public int capacity(Rect rect) {
        return rect.height();
      }

      @Override public int measure(View view) {
        return view.height();
      }

      @Override public int start(Rect rect) {
        return rect.top();
      }

      @Override public Rect span(Rect rect, int start, int size) {
        return rect.rows(start, size);
      }

      @Override public int overallHeight(int tally, int current) {
        return tally == Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : current == Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : tally + current;
      }

      @Override public int overallWidth(int tally, int current) {
        return Math.max(tally, current);
      }
    },

    COLUMNS() {
      @Override public int capacity(Rect rect) {
        return rect.width();
      }

      @Override public int measure(View view) {
        return view.width();
      }

      @Override public int start(Rect rect) {
        return rect.left();
      }

      @Override public Rect span(Rect rect, int start, int size) {
        return rect.columns(start, size);
      }

      @Override public int overallHeight(int tally, int current) {
        return Math.max(tally, current);
      }

      @Override public int overallWidth(int tally, int current) {
        return tally == Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : current == Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : tally + current;
      }
    };

    public abstract int capacity(Rect rect);

    public abstract int measure(View view);

    public abstract int start(Rect rect);

    public abstract Rect span(Rect rect, int start, int size);

    public abstract int overallHeight(int tally, int current);

    public abstract int overallWidth(int tally, int current);
  }

  public ViewGroup(Orientation orientation, View... children) {
    this(UUID.randomUUID().toString(), orientation, children);
  }

  ViewGroup(String id, Orientation orientation, View... children) {
    super(id);

    this.children = new LinkedList<>(Arrays.asList(children));
    this.orientation = orientation;
    this.rects = new LinkedHashMap<>();

    for (View child : children) {
      child.setInvalidationListener(this::invalidate);
    }
  }

  @Override protected void onLayout(Rect rect) {
    int capacity = orientation.capacity(rect);
    int flexible = 0;

    for (View child : children) {
      int size = orientation.measure(child);

      if (size == Integer.MAX_VALUE) {
        flexible++;
      } else {
        capacity -= size;
      }
    }

    Queue<Integer> flexibleSizes = new LinkedList<>();

    if (flexible != 0) {
      for (int i = 0; i < flexible - 1; i++) {
        int size = capacity / flexible;
        flexibleSizes.add(size);
        capacity -= size;
      }
      flexibleSizes.add(capacity);
    }

    int start = orientation.start(rect);

    for (View child : children) {
      int size = orientation.measure(child);

      if (size == Integer.MAX_VALUE) {
        size = flexibleSizes.remove();
      }

      rects.put(child.id(), orientation.span(rect, start, size));
      start += size;
    }

    for (View child : children) {
      child.layout(rects.get(child.id()));
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    for (View child : children) {
      child.draw(canvas.rect(rects.get(child.id())));
    }
  }

  @Override int height() {
    return children.stream()
        .mapToInt(View::height)
        .reduce(orientation::overallHeight)
        .orElse(Integer.MAX_VALUE);
  }

  @Override int width() {
    return children.stream()
        .mapToInt(View::width)
        .reduce(orientation::overallWidth)
        .orElse(Integer.MAX_VALUE);
  }

  @Override public <T> T find(String id, Class<T> viewClass) {
    T superResult = super.find(id, viewClass);
    return superResult != null
        ? superResult
        : children.stream()
            .map(v -> v.find(id, viewClass))
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
  }

  protected <T extends View> void update(List<String> updatedIds, Function<String, T> createView, Consumer<T> updateView) {
    // on add or remove, we need new layout! THIS IS THE CURRENT NPE PROBLEM.
    //   --> android calls this method requestLayout(). I imagine it would work like invalidate().
    // on overall shrinking, we need to somehow clear the old spots.
    //   maybe last row, column gets max height, and we fill self with spaces?
    final AtomicInteger cursor = new AtomicInteger(0);

    // commons-collections is huge, but it does let us keep from reimplementing
    // https://en.wikipedia.org/wiki/Diff#Algorithm
    // https://en.wikipedia.org/wiki/Longest_common_subsequence_problem
    new SequencesComparator<>(children.stream().map(View::id).collect(toList()), updatedIds)
        .getScript()
        .visit(new CommandVisitor<String>() {
          @Override public void visitInsertCommand(String id) {
            // Maybe play with (or think carefully through) the ordering here when we introduce requestLayout.
            // What to call before / after adding to the dom? (Wow, I said dom.)
            T newView = createView.apply(id);
            newView.setInvalidationListener(ViewGroup.this::invalidate);
            updateView.accept(newView);
            children.add(cursor.getAndIncrement(), newView);
          }

          @Override public void visitKeepCommand(String id) {
            // FIXME generify ViewGroup to avoid this warning
            updateView.accept((T) children.get(cursor.getAndIncrement()));
          }

          @Override public void visitDeleteCommand(String id) {
            children.remove(cursor.get()).setInvalidationListener(() -> {});
          }
        });
  }
}
