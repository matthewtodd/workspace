java_library(
    name = "console",
    testonly = True,
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/org/matthewtodd/perquackey/console",
        "//src/test/java/org/matthewtodd/perquackey",
        "@io_reactivex_rxjava2_rxjava",
        "@junit_junit",
        "@org_assertj_assertj_core",
        "@org_hamcrest_hamcrest_core",
        "@org_reactivestreams_reactive_streams",
    ],
)

java_test(
    name = "PerquackeyTest",
    size = "small",
    runtime_deps = [
        ":console",
    ],
)
