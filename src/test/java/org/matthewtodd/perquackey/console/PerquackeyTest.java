package org.matthewtodd.perquackey.console;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.TestScheduler;
import org.junit.Test;
import org.matthewtodd.console.StringDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class PerquackeyTest {
  @Test public void hookup() {
    BehaviorProcessor<Integer> input = BehaviorProcessor.create();
    BehaviorProcessor<Long> ticker = BehaviorProcessor.create();
    TestScheduler scheduler = new TestScheduler();
    StringDevice device = StringDevice.newBuilder().width(50).height(10).build();

    Perquackey.with(ticker)
        .on(input, scheduler::scheduleDirect, device)
        .start(() -> {});

    scheduler.triggerActions();
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
