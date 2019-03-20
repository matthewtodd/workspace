package org.matthewtodd.perquackey;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import org.matthewtodd.flow.Flow;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
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

  private final Collection<BitSet> possibleRolls;
  private final Processor<String, String> state = Flow.pipe("");
  private Letters letters = new Letters();

  Dice() {
    possibleRolls = new ArrayList<>();
    possibleRolls.add(noDiceSpokenFor());
  }

  void observe(Iterable<String> words) {
    letters = StreamSupport.stream(words.spliterator(), true)
        .map(word -> word.chars().collect(Letters::new, Letters::add, Letters::addAll))
        .reduce(new Letters(), Letters::union);
    possibleRolls.clear();
    possibleRolls.addAll(letters.possibleRollsFrom(singletonList(noDiceSpokenFor())));
    state.onNext(letters.toString());
  }

  boolean couldSpell(String word) {
    // TODO could short circuit. It's unnecessary here to find all the scenarios, just one.
    return !letters.newLettersIn(word).possibleRollsFrom(possibleRolls).isEmpty();
  }

  Publisher<String> state() {
    return state;
  }

  private static BitSet noDiceSpokenFor() {
    BitSet scenario = new BitSet(10); // TODO increase to 13 for vulnerable turns
    scenario.set(0, 10);
    return scenario;
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

  private static class Letters implements Iterable<Character> {
    private final byte[] buckets;

    private Letters() {
      this(new byte[26]);
    }

    private Letters(byte[] buckets) {
      this.buckets = buckets;
    }

    Collection<BitSet> possibleRollsFrom(Collection<BitSet> possibleRolls) {
      if (isEmpty()) {
        return possibleRolls;
      }

      char letter = head();
      Collection<BitSet> layer = new ArrayList<>();

      for (BitSet roll : possibleRolls) {
        for (int i = roll.nextSetBit(0); 0 <= i && i < roll.length(); i = roll.nextSetBit(i + 1)) {
          if (dice.get(i).couldProvide(letter)) {
            BitSet validRoll = (BitSet) roll.clone();
            validRoll.clear(i);
            layer.add(validRoll);
          }
        }
      }

      return tail().possibleRollsFrom(layer);
    }

    private Letters copy() {
      return new Letters(copyOf(buckets, buckets.length));
    }

    public Iterator<Character> iterator() {
      return new LettersIterator();
    }

    private class LettersIterator implements Iterator<Character> {
      private int index = 0;
      private int count = 0;

      @Override public boolean hasNext() {
        for (; index < buckets.length; index++) {
          if (count < buckets[index]) {
            count++;
            return true;
          } else {
            count = 0;
          }
        }
        return false;
      }

      @Override public Character next() {
        return (char) (index + 'a');
      }
    }

    void add(int letter) {
      buckets[letter - 'a']++;
    }

    void addAll(Letters other) {
      other.forEach(this::add);
    }

    boolean remove(int letter) {
      if (buckets[letter - 'a'] > 0) {
        buckets[letter - 'a']--;
        return true;
      } else {
        return false;
      }
    }

    boolean isEmpty() {
      return !iterator().hasNext();
    }

    Letters union(Letters other) {
      Letters result = copy();
      Letters scratchpad = copy();
      other.forEach(letter -> {
        if (!scratchpad.remove(letter)) {
          result.add(letter);
        }
      });
      return result;
    }

    Letters newLettersIn(String word) {
      Letters result = new Letters();
      Letters scratchpad = copy();

      for (int i = 0; i < word.length(); i++) {
        char letter = word.charAt(i);
        if (!scratchpad.remove(letter)) {
          result.add(letter);
        }
      }

      return result;
    }

    char head() {
      Iterator<Character> iterator = iterator();
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        throw new IllegalStateException();
      }
    }

    Letters tail() {
      Letters tail = copy();
      tail.remove(head());
      return tail;
    }

    @Override public String toString() {
      StringBuilder result = new StringBuilder();
      forEach(result::append);
      return result.toString();
    }
  }
}
