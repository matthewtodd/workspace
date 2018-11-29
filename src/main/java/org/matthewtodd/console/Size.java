package org.matthewtodd.console;

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
    },

    UNSPECIFIED {
      @Override int resolve(int spec, int requested) {
        return requested;
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

  static Size unspecified() {
    return new Size(Integer.MIN_VALUE, Mode.UNSPECIFIED);
  }

  private Size(int spec, Mode mode) {
    this.spec = spec;
    this.mode = mode;
  }

  int requesting(int requested) {
    return mode.resolve(spec, requested);
  }
}
