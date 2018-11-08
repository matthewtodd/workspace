package org.matthewtodd.perquackey.console;

import io.reactivex.processors.BehaviorProcessor;
import org.junit.Test;
import org.matthewtodd.console.Window.KeyPress;

public class ConsoleTest {
  @Test public void hookup() {
    BehaviorProcessor<KeyPress> input = BehaviorProcessor.create();
    BehaviorProcessor<Long> ticker = BehaviorProcessor.create();

    StringBuilder output = new StringBuilder();
    int rows = 10;
    int cols = 50;
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        output.append(" ");
      }
      output.append("\n");
    }

    //Console console = new Console(new Window(input, rows, cols, stroke -> output.replace()), ticker);
    //console.start();


  }
}
