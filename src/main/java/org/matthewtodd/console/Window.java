package org.matthewtodd.console;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import java.io.PrintStream;
import org.reactivestreams.Publisher;

public class Window {
  final FlowableProcessor<String> input = PublishProcessor.create();
  final PrintStream output;

  private View currentView = new View(Coordinator.NONE);

  public Window(Publisher<String> stdin, PrintStream output) {
    stdin.subscribe(input);
    this.output = output;
  }

  public void displayView(View view) {
    currentView.detach();
    currentView = view;
    currentView.attach(this);
  }
}
