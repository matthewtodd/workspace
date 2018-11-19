java_library(
  name = "perquackey",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
    "//src/test/java/org/matthewtodd/workflow",
    "//third_party:assertj",
    "//third_party:hamcrest",
    "//third_party:junit",
    "//third_party:reactive_streams",
    "//third_party:rxjava2",
  ],
  visibility = [
    "//src/test/java/org/matthewtodd/perquackey/console:__pkg__",
  ]
)

java_test(
  name = "TimerTest",
  size = "small",
  runtime_deps = [
    ":perquackey",
  ],
)

java_test(
  name = "TurnWorkflowTest",
  size = "small",
  runtime_deps = [
    ":perquackey",
  ],
)
