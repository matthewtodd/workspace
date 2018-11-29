java_library(
  name = "perquackey",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
    "//src/test/java/org/matthewtodd/workflow",
    "@org_assertj_assertj_core",
    "@org_hamcrest_hamcrest_core",
    "@junit_junit",
    "@org_reactivestreams_reactive_streams",
    "@io_reactivex_rxjava2_rxjava",
  ],
  testonly = True,
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
