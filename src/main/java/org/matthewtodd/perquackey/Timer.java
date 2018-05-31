package org.matthewtodd.perquackey;

import org.reactivestreams.Publisher;

interface Timer {
  void start();
  void stop();
  Publisher<Snapshot> snapshot();

  class Snapshot {
    private final boolean running;
    private final int remaining;
    private final int total;

    public Snapshot(boolean running, int remaining, int total) {
      this.running = running;
      this.remaining = remaining;
      this.total = total;
    }

    public boolean running() {
      return running;
    }

    public int remaining() {
      return remaining;
    }

    public int total() {
      return total;
    }
  }
}
