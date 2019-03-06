package org.matthewtodd.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import java.util.function.Consumer;

public abstract class View<SELF extends AbstractInteractableComponent<SELF> & CompositePanel>
    extends AbstractInteractableComponent<SELF> implements CompositePanel {

  private final Panel panel;
  private Consumer<SELF> addedListener = self -> { };
  private Consumer<SELF> removedListener = self -> { };

  protected View(LayoutManager layoutManager) {
    panel = new Panel(layoutManager);
    panel.setTheme(new SimpleTheme(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT));
  }

  @Override public final Panel getPanel() {
    return panel;
  }

  @Override public synchronized final void onAdded(Container container) {
    super.onAdded(container);
    addedListener.accept(self().takeFocus());
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

  @Override protected final InteractableRenderer<SELF> createDefaultRenderer() {
    return new InteractableRenderer<SELF>() {
      @Override public TerminalSize getPreferredSize(SELF component) {
        return component.getPanel().getPreferredSize();
      }

      @Override public void drawComponent(TextGUIGraphics graphics, SELF component) {
        component.getPanel().draw(graphics);
      }

      @Override public TerminalPosition getCursorLocation(SELF component) {
        return component.getCursorLocation();
      }
    };
  }
}
