package org.matthewtodd.perquackey;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.ObjectAssert;
import org.junit.Test;

public class TimerTest {
  @Test public void countsDownWhenStarted() {
    TimerTester
        .create().seePausedAt(2)
        .toggle().seeRunningAt(2)
        .tick().seeRunningAt(1)
        .tick().seeRunningAt(0).done();
  }

  @Test public void ignoresTicksUntilStarted() {
    TimerTester
        .create().seePausedAt(2)
        .tick().tick().tick()
        .toggle().seeRunningAt(2)
        .tick().seeRunningAt(1)
        .tick().seeRunningAt(0).done();
  }

  @Test public void ignoresTicksWhileStopped() {
    TimerTester
        .create().seePausedAt(2)
        .toggle().seeRunningAt(2)
        .tick().seeRunningAt(1)
        .stop().seePausedAt(1)
        .tick().tick().tick()
        .toggle().seeRunningAt(1)
        .tick().seeRunningAt(0).done();
  }

  @Test public void multipleSubscribers() {
    Timer timer = new Timer(180L);
    Flowable<Long> flowable = Flowable.fromPublisher(timer.state())
        .map(Timer.State::remaining);

    timer.toggle();
    TestSubscriber<Long> one = flowable.test();
    one.assertValues(180L);
    timer.tick(1);
    one.assertValues(180L, 179L);
    timer.tick(1);
    one.assertValues(180L, 179L, 178L);

    TestSubscriber<Long> two = flowable.test();
    two.assertValues(178L);
  }

  static class TimerTester {
    private final Timer timer;
    private final TestSubscriber<Timer.State> subscriber;
    private int index = 0;
    private int toggleCalls = 0;
    private int tickCalls = 0;

    static TimerTester create() {
      return new TimerTester(2L);
    }

    private TimerTester(long total) {
      timer = new Timer(total);
      subscriber = Flowable.fromPublisher(timer.state()).test();
    }

    TimerTester toggle() {
      checkForUnseenValues("toggle", ++toggleCalls);
      timer.toggle();
      return this;
    }

    TimerTester stop() {
      return toggle();
    }

    TimerTester tick() {
      checkForUnseenValues("tick", ++tickCalls);
      timer.tick(1);
      return this;
    }

    TimerTester seePausedAt(long remaining) {
      return see(remaining, false);
    }

    TimerTester seeRunningAt(long remaining) {
      return see(remaining, true);
    }

    void done() {
      subscriber.assertComplete();
    }

    private TimerTester see(long remaining, boolean running) {
      new ObjectAssert<>(subscriber.values().get(index++))
          .extracting(Timer.State::remaining, Timer.State::running)
          .containsExactly(remaining, running);
      return this;
    }

    private void checkForUnseenValues(String event, int eventCount) {
      if (subscriber.valueCount() > index) {
        throw new AssertionError(
            String.format("Extra timer snapshot values at %s #%d: %s", event, eventCount,
                subscriber.values().subList(index, subscriber.valueCount())));
      }
    }
  }
}
