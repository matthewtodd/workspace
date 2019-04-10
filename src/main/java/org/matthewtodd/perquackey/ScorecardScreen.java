package org.matthewtodd.perquackey;

import java.util.List;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class ScorecardScreen extends WorkflowScreen<ScorecardScreen.Data, ScorecardScreen.Events> {
  public static final String KEY = "ScorecardScreen";

  ScorecardScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {
    private final List<List<Integer>> scores;

    Data(List<List<Integer>> scores) {
      this.scores = scores;
    }

    public String playerName(int columnIndex) {
      return String.format("Player %d", columnIndex + 1);
    }

    public int playerCount() {
      return scores.size();
    }

    public Integer playerScore(int columnIndex, int rowIndex) {
      List<Integer> playerScores = scores.get(columnIndex);
      return rowIndex < playerScores.size() ? playerScores.get(rowIndex) : null;
    }

    public int scoreCount() {
      return scores.get(0).size();
    }
  }

  public interface Events {
    void numberOfPlayers(int numberOfPlayers);

    void nextTurn();

    void quit();
  }
}
