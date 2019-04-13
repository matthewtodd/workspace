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
import org.junit.Ignore;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;
import org.matthewtodd.workflow.WorkflowTester;

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
    // ieejosttuy
    workflow.turn(screen -> {
      screen.type("stout").enter(); // osttu
      screen.type("ties").enter(); // eiosttu
      screen.type("joys").enter(); // eijosttuy
      screen.assertThatWords().containsExactly("stout", "ties", "joys");
      screen.type("steel").enter(); // ! only 1 unknown letter left, A or E; steel needs E and L
      screen.assertThatWords().doesNotContain("steel");
    });
  }

  @Test public void inputRejected_impossibleToSpell_vulnerable() {
    // ieejosttuy + lmw
    workflow.vulnerableTurn(screen -> {
      screen.type("stout").enter(); // osttu
      screen.type("ties").enter(); // eiosttu
      screen.type("joys").enter(); // eijosttuy
      screen.assertThatWords().containsExactly("stout", "ties", "joys");
      screen.type("steel").enter(); // ! still 4 unknown letters left; we can make it work!
      screen.assertThatWords().contains("steel");
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
      workflow.tick();
      screen.assertThatTimeRemaining().isEqualTo(180);
      screen.toggleTimer();
      screen.assertThatTimeRemaining().isEqualTo(180);
      workflow.tick();
      screen.assertThatTimeRemaining().isEqualTo(179);
    });
  }

  @Test public void timeAnnouncement() {
    workflow.turn(screen -> {
      screen.toggleTimer();
      workflow.tick(179);
      workflow.assertThatAnnouncements().isEmpty();
      workflow.tick();
      workflow.assertThatAnnouncements().containsExactly(TimeIsUp);
    });
  }

  @Ignore("WIP")
  @Test public void timeAnnouncement_notAfterQuitting() {
    workflow.turn(screen -> {
      screen.toggleTimer();
      workflow.tick(179);
      workflow.assertThatAnnouncements().isEmpty();
      screen.quit();
    });

    workflow.tick();
    workflow.assertThatAnnouncements().isEmpty();
  }

  @Test public void resultIsScore() {
    workflow.turn(screen -> {
      screen.type("dog").enter();
      screen.quit();
    });
    workflow.assertThatResult().isEqualTo(60);
  }

  @Test public void resultIsScore_goingBack() {
    workflow.vulnerableTurn(TurnScreenTester::quit);
    workflow.assertThatResult().isEqualTo(-500);
  }

  private static class TurnWorkflowTester {
    private final BehaviorProcessor<Long> ticker;
    private final WorkflowTester<Boolean, Integer> workflow;
    private final List<Announcement> announcements;

    TurnWorkflowTester() {
      ticker = BehaviorProcessor.create();
      announcements = new ArrayList<>();
      workflow = new WorkflowTester<>(new TurnWorkflow(ticker, announcements::add));
    }

    void tick() {
      ticker.onNext(0L);
    }

    void tick(int times) {
      for (int i = 0; i < times; i++) {
        tick();
      }
    }

    void turn(Consumer<TurnScreenTester> assertions) {
      workflow.start(false);
      workflow.on(TurnScreen.class,
          (data, events) -> assertions.accept(new TurnScreenTester(data, events)));
    }

    void vulnerableTurn(Consumer<TurnScreenTester> assertions) {
      workflow.start(true);
      workflow.on(TurnScreen.class,
          (data, events) -> assertions.accept(new TurnScreenTester(data, events)));
    }

    AbstractIntegerAssert<?> assertThatResult() {
      return assertThat(workflow.result());
    }

    ListAssert<Announcement> assertThatAnnouncements() {
      return assertThat(announcements);
    }
  }

  private static class TurnScreenTester {
    private final AssertSubscriber<TurnScreen.Data> data;
    private final TurnScreen.Events events;

    TurnScreenTester(AssertSubscriber<TurnScreen.Data> data, TurnScreen.Events events) {
      this.data = data;
      this.events = events;
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

    void toggleTimer() {
      events.toggleTimer();
    }

    TurnScreenTester type(String word) {
      word.chars().forEach(c -> events.letter((char) c));
      return this;
    }
  }
}
