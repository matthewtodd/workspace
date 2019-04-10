package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.List;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class ScorecardScreen extends WorkflowScreen<ScorecardScreen.Data, ScorecardScreen.Events> {
  public static final String KEY = "ScorecardScreen";

  ScorecardScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {
    private final List<Integer> scores;

    Data(List<Integer> scores) {
      this.scores = Collections.unmodifiableList(scores);
    }

    public String playerName(int columnIndex) {
      return "Player 1";
    }

    public int playerCount() {
      return 1;
    }

    public List<Integer> playerScores(int columnIndex) {
      return scores;
    }

    public int scoreCount() {
      return scores.size();
    }
  }

  public interface Events {
    void nextTurn();

    void quit();
  }
}
