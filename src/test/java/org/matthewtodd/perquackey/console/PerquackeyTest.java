package org.matthewtodd.perquackey.console;

import io.reactivex.processors.BehaviorProcessor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PerquackeyTest {
  @Test public void hookup() {
    BehaviorProcessor<Integer> input = BehaviorProcessor.create();
    BehaviorProcessor<Long> ticker = BehaviorProcessor.create();
    StringDevice device = StringDevice.rows(10).columns(50).build();

    Perquackey.with(ticker)
        .on(input, Runnable::run, device)
        .start(() -> {});

    assertThat(device.toString().split("\n")).containsExactly(
        "0 points                             [paused] 3:00",
        "--------------------------------------------------",
        "3   4    5     6      7       8        9          ",
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
