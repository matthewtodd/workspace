package org.matthewtodd.console;

import java.util.Arrays;

public class AdapterView extends ViewGroup {
  private final Adapter adapter;

  public AdapterView(String id, Layout layout, Adapter adapter) {
    super(id, layout);
    this.adapter = adapter;
  }

  @Override Iterable<View> children() {
    return adapter.children();
  }

  public interface Adapter {
    Iterable<View> children();
  }

  public static Adapter staticChildren(View... children) {
    return () -> Arrays.asList(children);
  }
}
