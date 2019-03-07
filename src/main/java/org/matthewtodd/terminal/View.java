package org.matthewtodd.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComposite;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Composite;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import java.util.function.Consumer;

public abstract class View<SELF extends Container & Composite> extends AbstractComposite<SELF> {
  private Consumer<SELF> addedListener = self -> { };
  private Consumer<SELF> removedListener = self -> { };

  @Override public synchronized final void onAdded(Container container) {
    super.onAdded(container);
    addedListener.accept(self());
  }

  @Override public synchronized final void onRemoved(Container container) {
    removedListener.accept(self());
    super.onRemoved(container);
  }

  public final void setAddedListener(Consumer<SELF> addedListener) {
    this.addedListener = addedListener;
  }

  public final void setRemovedListener(Consumer<SELF> removedListener) {
    this.removedListener = removedListener;
  }

  @Override protected ComponentRenderer<SELF> createDefaultRenderer() {
    return new ComponentRenderer<SELF>() {
      @Override public TerminalSize getPreferredSize(SELF self) {
        return self.getComponent().getPreferredSize();
      }

      @Override public void drawComponent(TextGUIGraphics textGUIGraphics, SELF self) {
        self.getComponent().draw(textGUIGraphics);
      }
    };
  }
}
