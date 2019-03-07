package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

class Input {
  private final Processor<State, State> state = Flow.pipe(new State("", ""));
  private final StringBuilder buffer = new StringBuilder();
  private boolean valid = true;

  void append(char letter) {
    buffer.append(letter);
    state.onNext(snapshotState());
  }

  void chop() {
    buffer.setLength(Math.max(0, buffer.length() - 1));
    state.onNext(snapshotState());
  }

  String value() {
    return buffer.toString();
  }

  int length() {
    return buffer.length();
  }

  void markInvalid() {
    valid = false;
    state.onNext(snapshotState());
  }

  void markValid() {
    valid = true;
    state.onNext(snapshotState());
  }

  void reset() {
    buffer.setLength(0);
    valid = true;
    state.onNext(snapshotState());
  }

  Publisher<State> state() {
    return state;
  }

  private State snapshotState() {
    return new State(value(), valid ? "" : "too short");
  }

  public static class State {
    private String value;
    private String message;

    State(String value, String message) {
      this.value = value;
      this.message = message;
    }

    public String value() {
      return value;
    }

    public String message() {
      return message;
    }
  }
}
