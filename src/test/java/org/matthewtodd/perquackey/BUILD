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
        "//src/test/java/org/matthewtodd/flow",
        "//src/test/java/org/matthewtodd/workflow",
        "@maven//:io_reactivex_rxjava2_rxjava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_reactivestreams_reactive_streams",
    ],
)

java_test(
    name = "DictionaryTest",
    size = "small",
    runtime_deps = [
        ":perquackey",
    ],
)

java_test(
    name = "DiceTest",
    size = "small",
    runtime_deps = [
        ":perquackey",
    ],
)

java_test(
    name = "InputTest",
    size = "small",
    runtime_deps = [
        ":perquackey",
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
    name = "WordsTest",
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
