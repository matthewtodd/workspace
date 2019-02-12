package org.matthewtodd.terminal;

import com.googlecode.lanterna.gui2.Component;

public interface Coordinator<T extends Component> {
   void attach(T component);

   default void detach(T component) {}
}
