package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class TurnScreen extends WorkflowScreen<TurnScreen.Data, TurnScreen.Events> {
  public static final String KEY = "TurnScreen";

  TurnScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {
    private final WordList words;
    private final int score;
    private final Timer.State timer;
    private Input.State input;

    Data(WordList words, int score, Timer.State timer, Input.State input) {
      this.words = words;
      this.score = score;
      this.timer = timer;
      this.input = input;
    }

    public WordList words() {
      return words;
    }

    public int score() {
      return score;
    }

    public Timer.State timer() {
      return timer;
    }

    public Input.State input() {
      return input;
    }
  }

  public interface Events {
    void letter(char letter);

    void undoLetter();

    void word();

    void toggleTimer();

    void quit();
  }
}
