package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

class Input {
  private final Processor<String, String> state = Flow.pipe("");
  private final Processor<String, String> entries = Flow.pipe();
  private final StringBuilder buffer = new StringBuilder();

  void letter(char letter) {
    buffer.append(letter);
    state.onNext(buffer.toString());
  }

  void undoLetter() {
    buffer.setLength(Math.max(0, buffer.length() - 1));
    state.onNext(buffer.toString());
  }

  void enter() {
    if (buffer.length() >= 3) {
      entries.onNext(buffer.toString());
      buffer.setLength(0);
      state.onNext(buffer.toString());
    }
  }

  Publisher<String> state() {
    return state;
  }

  Publisher<String> entries() {
    return entries;
  }
}
