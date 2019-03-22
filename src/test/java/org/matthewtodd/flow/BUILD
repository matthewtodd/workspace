java_library(
    name = "flow",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey:__pkg__",
        "//src/test/java/org/matthewtodd/workflow:__pkg__",
    ],
    deps = [
        "@maven//:io_reactivex_rxjava2_rxjava",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_reactivestreams_reactive_streams",
    ],
)
