java_library(
  name = "perquackey",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
    "//src/test/java/org/matthewtodd/workflow",
    "@io_reactivex_rxjava2_rxjava//jar",
    "@junit_junit//jar",
    "@org_assertj_assertj_core//jar",
    "@org_hamcrest_hamcrest_core//jar",
    "@org_reactivestreams_reactive_streams//jar",
  ],
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
