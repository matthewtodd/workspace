package org.matthewtodd.perquackey.console;

import io.reactivex.functions.Action;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleTest {
  @Test public void hookup() {
    FlowableProcessor<String> input = BehaviorProcessor.create();
    FlowableProcessor<Long> ticker = BehaviorProcessor.create();
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    Consumer<Action> scheduler = action -> {
      try {
        action.run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    new Console(input, ticker, new PrintStream(output)).run(scheduler);

    ticker.onNext(1L);

    assertThat(output.toString().split("\n")).containsExactly(
        "",
        "",
        ""
    );
  }
}
