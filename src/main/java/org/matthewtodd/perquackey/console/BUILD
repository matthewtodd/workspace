java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/console",
    "//src/main/java/org/matthewtodd/flow",
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
  ],
  visibility = [
    "//src/test/java/org/matthewtodd/perquackey/console:__pkg__",
  ],
)

java_binary(
  name = "perquackey",
  runtime_deps = [
    ":console",
  ],
  main_class = "org.matthewtodd.perquackey.console.Console",
)
