package org.matthewtodd.perquackey.console;

import io.reactivex.processors.BehaviorProcessor;
import org.junit.Test;
import org.matthewtodd.console.Device;

import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleTest {
  @Test public void hookup() {
    BehaviorProcessor<Integer> input = BehaviorProcessor.create();
    BehaviorProcessor<Long> ticker = BehaviorProcessor.create();
    StringBuilder display = new StringBuilder();

    Device device = new Device() {
      @Override public int rows() {
        return 10;
      }

      @Override public int columns() {
        return 50;
      }

      @Override public void replace(int row, int column, String content) {
        int start = (row - 1) * (columns() + 1) + column - 1;
        int end = start + content.length();
        display.replace(start, end, content);
      }

      @Override public void clear() {
        display.setLength(0);
        display.append(join("\n", nCopies(rows(), join("", nCopies(columns(), " ")))));
      }
    };

    Console console = new Console(ticker, input, device);
    console.start(() -> {});

    assertThat(display.toString().split("\n")).containsExactly(
        "0 points                             [paused] 3:00",
        "--------------------------------------------------",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "                                                  ",
        "--------------------------------------------------",
        ":                                                 "
    );
  }
}
