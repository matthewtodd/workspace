package org.matthewtodd.workflow;

import org.reactivestreams.Publisher;

public interface Workflow<I, R> {
  void start(I input);

  Publisher<? extends WorkflowScreen<?, ?>> screen();

  Publisher<R> result();
}
