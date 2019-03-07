package org.matthewtodd.perquackey;

import io.reactivex.processors.BehaviorProcessor;
import org.junit.Test;
import org.matthewtodd.flow.AssertSubscriber;

public class TurnTest {
  // deleting a word / undo
  // auto-remove plural on spelling singular
  // auto-remove singular on spelling plural
  // auto-pluralize when filling category with s available
  @Test public void removesSingularWhenSpellingPlural() {
    AssertSubscriber<TurnScreen.Data> snapshot = AssertSubscriber.create();
    // TODO want to wrap timing around the turn after all, I think!
    Turn turn = new Turn(new Timer(180, BehaviorProcessor.create()));
    turn.snapshot().subscribe(snapshot);
    turn.letter('d');
    turn.letter('o');
    turn.letter('g');
    turn.word();
    snapshot.assertThat(TurnScreen.Data::words).containsExactly("dog");
    turn.letter('d');
    turn.letter('o');
    turn.letter('g');
    turn.letter('s');
    turn.word();
    snapshot.assertThat(TurnScreen.Data::words).containsExactly("dogs");
  }

  @Test public void removesPluralWhenSpellingSingular() {
    AssertSubscriber<TurnScreen.Data> snapshot = AssertSubscriber.create();
    Turn turn = new Turn(new Timer(180, BehaviorProcessor.create()));
    turn.snapshot().subscribe(snapshot);
    turn.letter('d');
    turn.letter('o');
    turn.letter('g');
    turn.letter('s');
    turn.word();
    snapshot.assertThat(TurnScreen.Data::words).containsExactly("dogs");
    turn.letter('d');
    turn.letter('o');
    turn.letter('g');
    turn.word();
    snapshot.assertThat(TurnScreen.Data::words).containsExactly("dog");
  }

  @Test public void rejectsWordsThatAreTooShort() {
    AssertSubscriber<TurnScreen.Data> snapshot = AssertSubscriber.create();
    Turn turn = new Turn(new Timer(180, BehaviorProcessor.create()));
    turn.snapshot().subscribe(snapshot);
    turn.letter('z');
    turn.letter('a');
    turn.word();
    snapshot.assertThat(TurnScreen.Data::words).isEmpty();
  }
}
