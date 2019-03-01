java_library(
    name = "terminal",
    testonly = True,
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/org/matthewtodd/perquackey/terminal",
        "//src/main/java/org/matthewtodd/terminal",
        "@com_googlecode_lanterna_lanterna",
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
        ":terminal",
    ],
)
