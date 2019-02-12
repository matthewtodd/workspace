package org.matthewtodd.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComposite;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Composite;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public abstract class CoordinatorView<SELF extends Container & Composite>
    extends AbstractComposite<SELF> {

  private final Coordinator<SELF> coordinator;

  public CoordinatorView(Coordinator<SELF> coordinator) {
    this.coordinator = coordinator;
  }

  @Override public synchronized void onAdded(Container container) {
    super.onAdded(container);
    coordinator.attach(self());
  }

  @Override public synchronized void onRemoved(Container container) {
    coordinator.detach(self());
    super.onRemoved(container);
  }

  @Override protected ComponentRenderer<SELF> createDefaultRenderer() {
    return new ComponentRenderer<SELF>() {
      @Override public TerminalSize getPreferredSize(SELF component) {
        return component.getComponent().getPreferredSize();
      }

      @Override public void drawComponent(TextGUIGraphics graphics, SELF component) {
        component.getComponent().draw(graphics);
      }
    };
  }
}
