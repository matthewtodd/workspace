java_library(
    name = "flow",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey:__pkg__",
        "//src/test/java/org/matthewtodd/workflow:__pkg__",
    ],
    deps = [
        "@io_reactivex_rxjava2_rxjava",
        "@org_assertj_assertj_core",
        "@org_reactivestreams_reactive_streams",
    ],
)
