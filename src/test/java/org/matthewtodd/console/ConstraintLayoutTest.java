package org.matthewtodd.console;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthewtodd.console.ConstraintLayout.Constraint.constrain;

public class ConstraintLayoutTest {
  @Test public void fullscreen() {
    StringDevice device = StringDevice.newBuilder().width(6).height(3).build();

    HorizontalRule dashes = new HorizontalRule("dashes", '-');

    View view = new AdapterView("fullscreen",
        new ConstraintLayout(
            constrain(dashes.id).top().toTopOfParent(),
            constrain(dashes.id).left().toLeftOfParent(),
            constrain(dashes.id).right().toRightOfParent(),
            constrain(dashes.id).bottom().toBottomOfParent()),
        AdapterView.staticChildren(dashes));

    view.measure(device.measuredWidth(), device.measuredHeight());
    view.layout(device.rect());
    view.draw(device.canvas());

    assertThat(device).containsExactly(
        "------",
        "------",
        "------"
    );
  }

  @Test public void fillRemainingHorizontal() {
    StringDevice device = StringDevice.newBuilder().width(6).height(3).build();

    HorizontalRule dashes = new HorizontalRule("dashes", '-');
    HorizontalRule stars = new HorizontalRule("stars", '*');

    View view = new AdapterView("balancedHorizontally",
        new ConstraintLayout(
            constrain(dashes.id).top().toTopOfParent(),
            constrain(dashes.id).left().toLeftOfParent(),
            constrain(dashes.id).width().fixed(2),
            constrain(dashes.id).bottom().toBottomOfParent(),
            constrain(stars.id).top().toTopOfParent(),
            constrain(stars.id).left().toRightOf(dashes.id),
            constrain(stars.id).right().toRightOfParent(),
            constrain(stars.id).bottom().toBottomOfParent()),
        AdapterView.staticChildren(
            dashes,
            stars));

    view.measure(device.measuredWidth(), device.measuredHeight());
    view.layout(device.rect());
    view.draw(device.canvas());

    assertThat(device).containsExactly(
        "--****",
        "--****",
        "--****"
    );
  }
}
