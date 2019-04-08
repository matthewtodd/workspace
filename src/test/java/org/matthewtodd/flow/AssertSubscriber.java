package org.matthewtodd.flow;

import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertSubscriber<T> implements Subscriber<T> {
  private final TestSubscriber<T> delegate;
  private final String description; // TODO rewrite assertions to call assertThat().as(description)

  private AssertSubscriber(String description) {
    this.description = description;
    delegate = TestSubscriber.create();
  }

  public static <T> AssertSubscriber<T> create(String description) {
    return new AssertSubscriber<>(description);
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
    assertThat(delegate.values()).as("%s values", description).isNotEmpty();
    return delegate.values().get(delegate.values().size() - 1);
  }

  public void assertComplete() {
    delegate.assertComplete();
  }

  public void assertNotComplete() {
    assertThat(delegate.completions() > 0).as("%s complete", description).isFalse();
  }

  public void assertEmpty() {
    delegate.assertEmpty();
  }

  public void assertValueCount(int i) {
    delegate.assertValueCount(i);
  }

  @SafeVarargs public final void assertValues(T... values) {
    delegate.assertValues(values);
  }
}
