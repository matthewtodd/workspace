package org.matthewtodd.console;

import java.util.function.Function;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TableLayoutTest {
  @Test public void hookup() {
    StringDevice device = StringDevice.newBuilder()
        .width(6)
        .height(3)
        .build();

    View view = new AdapterView("table",
        new TableLayout(),
        new AdapterView.Adapter() {
          @Override public Iterable<View> children() {
            return null;
          }
        });

    view.measure(device.measuredWidth(), device.measuredHeight());
    view.layout(device.rect());
    view.draw(device.canvas());

    assertThat(device).containsExactly(
        "------",
        "------",
        "------"
    );

  }

  private class TableLayout implements Layout {
    // 1. Given this API, the layout needs some OOB way of knowing what the column ids are.
    //    Perhaps the adapter provides them somehow, along with a notification when they change.
    //    How does the layout become aware of the adapter?
    //    And what here, in the layout, is actually table-specific?
    @Override public void measure(Size width, Size height, Function<String, View> finder) {
      // for each column id in L->R order
      //   measure with (trimmed) at most width? Or with a fixed width provided by the adapter?
    }

    @Override public void layout(Rect bounds, Function<String, View> finder) {
      // for each column in L->R order
      //   layout with some kind of clipped bounds, maybe stored from before?
    }
  }
}
