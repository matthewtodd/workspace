package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class PausedScreen extends WorkflowScreen<Turn.Snapshot, PausedScreen.Events> {
  public static final String KEY = "PausedScreen";

  public PausedScreen(Publisher<Turn.Snapshot> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  interface Events {
    void resumeTimer();
  }
}
