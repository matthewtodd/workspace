package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

class Dice {
  private static final List<Die> dice = unmodifiableList(asList(
      Die.of("aaaeee"),
      Die.of("aaaeee"),
      Die.of("bhikrt"),
      Die.of("bloowy"),
      Die.of("cmoopw"),
      Die.of("dlnort"),
      Die.of("ejqvxz"),
      Die.of("fhirsu"),
      Die.of("finptu"),
      Die.of("gimrsu"),
      // vulnerable:
      Die.of("bfhlnp"),
      Die.of("cdgjkm"),
      Die.of("qssvwy")
  ));

  private final Collection<BitSet> scenarios;
  private final Processor<String, String> state = Flow.pipe("");
  private String letters = "";

  Dice() {
    scenarios = new ArrayList<>();
    scenarios.add(baseScenario());
  }

  public void observe(Iterable<String> words) {
    // TODO ertain a Letters object, not the string.
    letters = StreamSupport.stream(words.spliterator(), true)
        .map(word -> word.chars().collect(Letters::new, Letters::add, Letters::addAll))
        .reduce(new Letters(), Letters::max)
        .toString();
    scenarios.clear();
    // TODO pass a Letters object
    scenarios.addAll(calculateScenarios(letters, singletonList(baseScenario())));
    // TODO emit letters.toString();
    state.onNext(letters);
  }

  boolean couldSpell(String word) {
    // TODO could short circuit. It's unnecessary here to find all the scenarios, just one.
    // TODO make letters.without(), except the name is wrong.
    return !calculateScenarios(without(letters, word), scenarios).isEmpty();
  }

  public Publisher<String> state() {
    return state;
  }

  private static BitSet baseScenario() {
    BitSet scenario = new BitSet(10); // TODO increase to 13 for vulnerable turns
    scenario.set(0, 10);
    return scenario;
  }

  private static Collection<BitSet> calculateScenarios(String letters,
      Collection<BitSet> scenarios) {
    if (letters.isEmpty()) {
      return scenarios;
    }

    char letter = letters.charAt(0);
    Collection<BitSet> layer = new ArrayList<>();

    for (BitSet scenario : scenarios) {
      for (int i = scenario.nextSetBit(0); 0 <= i && i < scenario.length();
          i = scenario.nextSetBit(i + 1)) {
        if (dice.get(i).couldProvide(letter)) {
          BitSet validScenario = (BitSet) scenario.clone();
          validScenario.clear(i);
          layer.add(validScenario);
        }
      }
    }

    return calculateScenarios(letters.substring(1), layer);
  }

  private static String without(String letters, String word) {
    StringBuilder mutableLetters = new StringBuilder(letters);
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < word.length(); i++) {
      String letter = word.substring(i, i + 1);
      int index = mutableLetters.indexOf(letter);
      if (index >= 0) {
        mutableLetters.setCharAt(index, '!');
      } else {
        result.append(letter);
      }
    }

    return result.toString();
  }

  private static class Die {
    private final char[] faces;

    Die(char[] faces) {
      this.faces = faces;
    }

    static Die of(String letters) {
      return new Die(letters.toCharArray());
    }

    boolean couldProvide(char letter) {
      for (char face : faces) {
        if (face == letter) {
          return true;
        }
      }
      return false;
    }
  }

  private static class Letters {
    private final byte[] buckets;

    private Letters() {
      this(new byte[26]);
    }

    private Letters(byte[] buckets) {
      this.buckets = buckets;
    }

    void add(int letter) {
      buckets[letter - 'a']++;
    }

    void addAll(Letters other) {
      for (int i = 0; i < other.buckets.length; i++) {
        buckets[i] += other.buckets[i];
      }
    }

    Letters max(Letters other) {
      byte[] buckets = new byte[26];
      for (int i = 0; i < buckets.length; i++) {
        buckets[i] = (byte) Math.max(this.buckets[i], other.buckets[i]);
      }
      return new Letters(buckets);
    }

    @Override public String toString() {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < buckets.length; i++) {
        for (int j = 0; j < buckets[i]; j++) {
          result.append((char) ('a' + i));
        }
      }
      return result.toString();
    }
  }
}
