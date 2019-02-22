package org.matthewtodd.flow;

import io.reactivex.subscribers.TestSubscriber;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.assertj.core.api.IntegerAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
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

  public T currentValue() {
    return delegate.values().get(delegate.values().size() - 1);
  }

  public IntegerAssert assertThat(ToIntFunction<T> extractor) {
    return new IntegerAssert(extractor.applyAsInt(currentValue()));
  }

  public <U> IterableAssert<U> assertThat(ToIterableFunction<T, U> extractor) {
    return new IterableAssert<>(extractor.apply(currentValue()));
  }

  public <U> ObjectAssert<U> assertThat(Function<T, U> extractor) {
    return new ObjectAssert<>(extractor.apply(currentValue()));
  }

  public void assertComplete() {
    delegate.assertComplete();
  }

  public void assertNotComplete() {
    delegate.assertNotComplete();
  }

  public void assertValueCount(int i) {
    delegate.assertValueCount(i);
  }

  public interface ToIterableFunction<T, U> {
    Iterable<U> apply(T value);
  }
}
