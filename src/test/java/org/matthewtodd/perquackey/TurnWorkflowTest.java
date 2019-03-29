package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;
import org.matthewtodd.workflow.WorkflowTester;
import org.reactivestreams.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthewtodd.perquackey.Announcement.TimeIsUp;

public class TurnWorkflowTest {
  private TurnWorkflowTester workflow;

  @Before public void setUp() {
    workflow = new TurnWorkflowTester();
  }

  @Test public void words() {
    workflow.turn(screen -> {
      screen.assertThatWords().isEmpty();
      screen.type("dog").enter();
      screen.assertThatWords().containsExactly("dog");
    });
  }

  @Test public void letters() {
    workflow.turn(screen -> {
      screen.assertThatLetters().isEmpty();
      screen.type("dog").enter();
      screen.assertThatLetters().isEqualTo("dgo");
    });
  }

  @Test public void inputMinLength() {
    workflow.turn(screen -> {
      screen.assertThatInput().isEmpty();
      screen.type("dog");
      screen.assertThatInput().isEqualTo("dog");
      screen.enter();
      screen.assertThatInput().isEmpty();
    });
  }

  @Test public void inputMinLength_vulnerable() {
    workflow.vulnerableTurn(screen -> {
      screen.type("dogs").enter();
      screen.assertThatWords().isNotEmpty();
    });
  }

  @Test public void inputRejected() {
    workflow.turn(screen -> {
      screen.type("a").enter();
      screen.assertThatInput().isEqualTo("a");
      screen.assertThatWords().isEmpty();
    });
  }

  @Test public void inputRejected_tooShort() {
    workflow.turn(screen -> {
      screen.type("do").enter();
      screen.assertThatWords().isEmpty();
    });
  }

  @Test public void inputRejected_tooShort_vulnerable() {
    workflow.vulnerableTurn(screen -> {
      screen.type("dog").enter();
      screen.assertThatWords().isEmpty();
    });
  }

  @Test public void inputRejected_notInDictionary() {
    workflow.turn(screen -> {
      screen.type("glarb").enter();
      screen.assertThatWords().isEmpty();
    });
  }

  @Test public void inputRejected_impossibleToSpell() {
    // aiejoottuy
    workflow.turn(screen -> {
      screen.type("tout").enter(); // ottu
      screen.type("tie").enter(); // eiottu
      screen.type("joy").enter(); // eijottuy
      screen.assertThatWords().containsExactly("tout", "tie", "joy");
      screen.type("ran").enter(); // ! only 2 unknown letters left; "ran" needs 3
      screen.assertThatWords().doesNotContain("ran");
    });
  }

  @Test public void score() {
    workflow.turn(screen -> {
      screen.assertThatScore().isZero();
      screen.type("dog").enter();
      screen.assertThatScore().isEqualTo(60);
    });
  }

  @Test public void timer() {
    workflow.turn(screen -> {
      screen.assertThatTimeRemaining().isEqualTo(180);
      screen.tick();
      screen.assertThatTimeRemaining().isEqualTo(180);
      screen.toggleTimer();
      screen.assertThatTimeRemaining().isEqualTo(180);
      screen.tick();
      screen.assertThatTimeRemaining().isEqualTo(179);
    });
  }

  @Test public void timeAnnouncement() {
    workflow.turn(screen -> {
      screen.toggleTimer();
      screen.tick(179);
      workflow.assertThatAnnouncements().isEmpty();
      screen.tick();
      workflow.assertThatAnnouncements().containsExactly(TimeIsUp);
    });
  }

  @Test public void quitting() {
    workflow.turn(TurnScreenTester::quit);
    workflow.assertThatResult().isEmpty();
  }

  private static class TurnWorkflowTester {
    private final BehaviorProcessor<Long> ticker;
    private final WorkflowTester<Boolean, Words.State> workflow;
    private final List<Announcement> announcements;

    TurnWorkflowTester() {
      ticker = BehaviorProcessor.create();
      announcements = new ArrayList<>();
      workflow = new WorkflowTester<>(new TurnWorkflow(ticker, announcements::add));
    }

    void turn(Consumer<TurnScreenTester> assertions) {
      workflow.start(false);
      workflow.on(TurnScreen.class,
          (data, events) -> assertions.accept(new TurnScreenTester(data, events, ticker)));
    }

    void vulnerableTurn(Consumer<TurnScreenTester> assertions) {
      workflow.start(true);
      workflow.on(TurnScreen.class,
          (data, events) -> assertions.accept(new TurnScreenTester(data, events, ticker)));
    }

    IterableAssert<String> assertThatResult() {
      return assertThat(workflow.result());
    }

    ListAssert<Announcement> assertThatAnnouncements() {
      return assertThat(announcements);
    }
  }

  private static class TurnScreenTester {
    private final AssertSubscriber<TurnScreen.Data> data;
    private final TurnScreen.Events events;
    private final Subscriber<Long> ticker;

    TurnScreenTester(AssertSubscriber<TurnScreen.Data> data, TurnScreen.Events events,
        Subscriber<Long> ticker) {
      this.data = data;
      this.events = events;
      this.ticker = ticker;
    }

    AbstractCharSequenceAssert<?, String> assertThatInput() {
      return assertThat(data.get().input());
    }

    AbstractCharSequenceAssert<?, String> assertThatLetters() {
      return assertThat(data.get().knownLetters());
    }

    AbstractIntegerAssert<?> assertThatScore() {
      return assertThat(data.get().score());
    }

    AbstractLongAssert<?> assertThatTimeRemaining() {
      return assertThat(data.get().timer().remaining());
    }

    IterableAssert<String> assertThatWords() {
      return assertThat(data.get().words());
    }

    void enter() {
      events.word();
    }

    void quit() {
      events.quit();
    }

    void tick() {
      ticker.onNext(0L);
    }

    void tick(int times) {
      for (int i = 0; i < times; i++) {
        tick();
      }
    }

    void toggleTimer() {
      events.toggleTimer();
    }

    TurnScreenTester type(String word) {
      word.chars().forEach(c -> events.letter((char) c));
      return this;
    }
  }
}
