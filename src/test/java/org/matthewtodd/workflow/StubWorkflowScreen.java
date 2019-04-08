package org.matthewtodd.workflow;

import org.reactivestreams.Publisher;

public class StubWorkflowScreen extends WorkflowScreen<Void, StubWorkflowScreen.Events> {
  StubWorkflowScreen(Publisher<Void> screenData, Events eventHandler) {
    super("StubWorkFlowScreen", screenData, eventHandler);
  }

  public interface Events {
    void assertInputIsEqualTo(Object input);
    void result(Object result);
  }
}
