package org.matthewtodd.perquackey;

import org.matthewtodd.workflow.WorkflowScreen;
import org.reactivestreams.Publisher;

public class SummaryScreen extends WorkflowScreen<SummaryScreen.Data, SummaryScreen.Events> {
  public static final String KEY = "SummaryScreen";

  public SummaryScreen(Publisher<Data> screenData, Events eventHandler) {
    super(KEY, screenData, eventHandler);
  }

  public static class Data {

  }

  public interface Events {
    void quit();
  }
}
