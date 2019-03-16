package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class TurnScreen extends WorkflowScreen<TurnScreen.Data, TurnScreen.Events> {
  public static final String KEY = "TurnScreen";

  TurnScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {
    private final Words.State words;
    private final int score;
    private final String letters;
    private final Timer.State timer;
    private final String input;

    Data(Words.State words, int score, String letters, Timer.State timer, String input) {
      this.words = words;
      this.score = score;
      this.letters = letters;
      this.timer = timer;
      this.input = input;
    }

    public Words.State words() {
      return words;
    }

    public int score() {
      return score;
    }

    public String letters() {
      return letters;
    }

    public Timer.State timer() {
      return timer;
    }

    public String input() {
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
