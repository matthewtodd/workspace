java_library(
    name = "console",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey/console:__pkg__"
    ],
    deps = [
        "//src/main/java/org/matthewtodd/console",
        "@junit_junit",
        "@org_assertj_assertj_core",
        "@org_hamcrest_hamcrest_core",
    ],
)

java_test(
    name = "ConstraintLayoutTest",
    size = "small",
    runtime_deps = [
        ":console",
    ],
)

java_test(
    name = "TableLayoutTest",
    size = "small",
    runtime_deps = [
        ":console",
    ],
)
