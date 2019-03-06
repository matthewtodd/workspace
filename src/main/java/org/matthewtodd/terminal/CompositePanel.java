package org.matthewtodd.terminal;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.LayoutData;
import com.googlecode.lanterna.gui2.Panel;

interface CompositePanel {
  Panel getPanel();

  default void addComponent(LayoutData layoutData, Component component) {
    getPanel().addComponent(component, layoutData);
  }
}
