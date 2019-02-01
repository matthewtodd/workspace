package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class TurnScreen extends WorkflowScreen<Turn.Snapshot, TurnScreen.Events> {
  public static final String KEY = "TurnScreen";

  TurnScreen(Publisher<Turn.Snapshot> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public interface Events {
    void spell(String word);

    void toggleTimer();
  }
}
