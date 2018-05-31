package org.matthewtodd.workflow;

import org.reactivestreams.Publisher;

public abstract class WorkflowScreen<D, E> {
  public final String key;
  public final Publisher<D> screenData;
  public final E eventHandler;

  public WorkflowScreen(String key, Publisher<D> screenData, E eventHandler) {
    this.key = key;
    this.screenData = screenData;
    this.eventHandler = eventHandler;
  }
}
