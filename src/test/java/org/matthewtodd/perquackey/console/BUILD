java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/console",
    "//src/main/java/org/matthewtodd/perquackey/console",
    "//src/test/java/org/matthewtodd/perquackey",
    "@org_assertj_assertj_core",
    "@org_hamcrest_hamcrest_core",
    "@junit_junit",
    "@org_reactivestreams_reactive_streams",
    "@io_reactivex_rxjava2_rxjava",
  ],
  testonly = True,
)

java_test(
  name = "PerquackeyTest",
  size = "small",
  runtime_deps = [
    ":console",
  ],
)
