package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class SpellingScreen extends WorkflowScreen<Turn.Snapshot, SpellingScreen.Events> {
  public static final String KEY = "SpellingScreen";

  public SpellingScreen(Publisher<Turn.Snapshot> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public interface Events {
    void spell(String word);

    void pauseTimer();
  }
}
