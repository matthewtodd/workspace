java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/perquackey/console",
    "@io_reactivex_rxjava2_rxjava//jar",
    "@junit_junit//jar",
    "@org_assertj_assertj_core//jar",
    "@org_hamcrest_hamcrest_core//jar",
    "@org_reactivestreams_reactive_streams//jar",
  ],
)

java_test(
  name = "ConsoleTest",
  size = "small",
  runtime_deps = [
    ":console",
  ],
)
