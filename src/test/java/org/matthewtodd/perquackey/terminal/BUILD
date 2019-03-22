java_library(
    name = "terminal",
    testonly = True,
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/org/matthewtodd/perquackey/terminal",
        "//src/main/java/org/matthewtodd/terminal",
        "@maven//:com_googlecode_lanterna_lanterna",
        "@maven//:io_reactivex_rxjava2_rxjava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_reactivestreams_reactive_streams",
    ],
)

java_test(
    name = "PerquackeyTest",
    size = "small",
    runtime_deps = [
        ":terminal",
    ],
)

java_test(
    name = "TurnViewTest",
    size = "small",
    runtime_deps = [
        ":terminal",
    ],
)
