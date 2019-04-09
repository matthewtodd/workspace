package org.matthewtodd.perquackey;

import java.util.Collections;
import java.util.List;
import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class SummaryScreen extends WorkflowScreen<SummaryScreen.Data, SummaryScreen.Events> {
  public static final String KEY = "SummaryScreen";

  SummaryScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {
    private final Integer score;

    Data(Integer score) {
      this.score = score;
    }

    public String playerName(int columnIndex) {
      return "Player 1";
    }

    public int playerCount() {
      return 1;
    }

    public List<Integer> playerScores(int columnIndex) {
      return Collections.singletonList(score);
    }

    public int scoreCount() {
      return 1;
    }
  }

  public interface Events {
    void quit();
  }
}
