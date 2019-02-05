java_library(
    name = "perquackey",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey/terminal:__pkg__",
    ],
    deps = [
        "//src/main/java/org/matthewtodd/perquackey",
        "//src/main/java/org/matthewtodd/workflow",
        "//src/test/java/org/matthewtodd/workflow",
        "@io_reactivex_rxjava2_rxjava",
        "@junit_junit",
        "@org_assertj_assertj_core",
        "@org_hamcrest_hamcrest_core",
        "@org_reactivestreams_reactive_streams",
    ],
)

java_test(
    name = "ScorerTest",
    size = "small",
    runtime_deps = [
        ":perquackey",
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
