package org.matthewtodd.console;

import static java.lang.Integer.MAX_VALUE;

final class Size {
  private final int spec;
  private final Mode mode;

  private enum Mode {
    AT_MOST {
      @Override int resolve(int spec, int requested) {
        return Math.min(spec, requested);
      }
    },

    EXACTLY {
      @Override int resolve(int spec, int requested) {
        return spec;
      }
    };

    abstract int resolve(int spec, int requested);
  }

  static Size atMost(int limit) {
    return new Size(limit, Mode.AT_MOST);
  }
  static Size exactly(int size) {
    return new Size(size, Mode.EXACTLY);
  }

  private Size(int spec, Mode mode) {
    this.spec = spec;
    this.mode = mode;
  }

  int requesting(int requested) {
    return mode.resolve(spec, requested);
  }

  int available() {
    return requesting(MAX_VALUE);
  }

  // TODO pull the AT_MOST out into Size#atMost(), maybe? Not sure if this coupling is inherent in the act.
  Size trim(int start, int end) {
    return new Size(end - start, Mode.AT_MOST);
  }
}
