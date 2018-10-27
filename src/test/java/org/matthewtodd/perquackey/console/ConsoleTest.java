package org.matthewtodd.perquackey.console;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleTest {
  @Test public void hookup() {
    FlowableProcessor<String> input = BehaviorProcessor.create();
    FlowableProcessor<Long> ticker = BehaviorProcessor.create();
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    new Console(input, ticker, new PrintStream(output)).start();

    ticker.onNext(1L);

    assertThat(output.toString().split("\n")).containsExactly(
        "",
        "",
        ""
    );
  }
}
