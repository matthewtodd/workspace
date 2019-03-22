java_library(
    name = "workflow",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey:__pkg__",
    ],
    deps = [
        "//src/main/java/org/matthewtodd/workflow",
        "//src/test/java/org/matthewtodd/flow",
        "@maven//:io_reactivex_rxjava2_rxjava",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_reactivestreams_reactive_streams",
    ],
)
