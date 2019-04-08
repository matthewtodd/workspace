package org.matthewtodd.perquackey.terminal;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import org.matthewtodd.terminal.CommandLine;
import org.matthewtodd.terminal.View;

class SummaryView extends View<SummaryView> {
  final CommandLine commandLine = new CommandLine();

  SummaryView() {
    setComponent(new Panel(new BorderLayout())
        .addComponent(new Panel(new GridLayout(1).setLeftMarginSize(0).setRightMarginSize(0))
            .addComponent(commandLine, GridLayout.createHorizontallyFilledLayoutData(1))
            .setLayoutData(BorderLayout.Location.BOTTOM)));
  }
}
