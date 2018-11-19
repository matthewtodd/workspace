package org.matthewtodd.perquackey;

public class a {
  public static TurnBuilder turn() {
    return new TurnBuilder();
  }

  public static class TurnBuilder {
    private WordList words = WordList.EMPTY;
    private int score = 0;
    private TimerBuilder timer = new TimerBuilder();

    public Turn.Snapshot build() {
      return new Turn.Snapshot(words, score, timer.build());
    }

    public TurnBuilder w(String word) {
      words.add(word);
      return this;
    }
  }

  private static class TimerBuilder {
    public Timer.Snapshot build() {
      return new Timer.Snapshot(180L, 0L, false);
    }
  }
}
