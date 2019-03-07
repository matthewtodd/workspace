package org.matthewtodd.flow;

import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class AssertSubscriber<T> implements Subscriber<T> {
  private final TestSubscriber<T> delegate;

  private AssertSubscriber() {
    delegate = TestSubscriber.create();
  }

  public static <T> AssertSubscriber<T> create() {
    return new AssertSubscriber<>();
  }

  @Override public void onSubscribe(Subscription s) {
    delegate.onSubscribe(s);
  }

  @Override public void onNext(T t) {
    delegate.onNext(t);
  }

  @Override public void onError(Throwable t) {
    delegate.onError(t);
  }

  @Override public void onComplete() {
    delegate.onComplete();
  }

  public T get() {
    return delegate.values().get(delegate.values().size() - 1);
  }

  public void assertComplete() {
    delegate.assertComplete();
  }

  public void assertNotComplete() {
    delegate.assertNotComplete();
  }

  public void assertEmpty() {
    delegate.assertEmpty();
  }

  public void assertValueCount(int i) {
    delegate.assertValueCount(i);
  }
}
