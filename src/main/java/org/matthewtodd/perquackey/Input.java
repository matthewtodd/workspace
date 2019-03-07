package org.matthewtodd.perquackey;

import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

class Input {
  private final Processor<State, State> state = Flow.pipe(new State("", ""));
  private final Processor<String, String> words = Flow.pipe();
  private final StringBuilder buffer = new StringBuilder();
  private boolean valid = true;

  void letter(char letter) {
    buffer.append(letter);
    state.onNext(snapshotState());
    if (buffer.length() >= 3) {
      valid = true;
      state.onNext(snapshotState());
    }
  }

  void undoLetter() {
    buffer.setLength(Math.max(0, buffer.length() - 1));
    state.onNext(snapshotState());
  }

  void word() {
    if (buffer.length() < 3) {
      valid = false;
      state.onNext(snapshotState());
    } else {
      words.onNext(buffer.toString());
      buffer.setLength(0);
      valid = true;
      state.onNext(snapshotState());
    }
  }

  Publisher<State> state() {
    return state;
  }

  Publisher<String> words() {
    return words;
  }

  private State snapshotState() {
    return new State(buffer.toString(), valid ? "" : "too short");
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
