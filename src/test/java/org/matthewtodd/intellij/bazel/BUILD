java_library(
    name = "bazel",
    testonly = True,
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/main/java/org/matthewtodd/intellij/bazel",
        "//src/test/java/org/matthewtodd/intellij",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)

java_test(
    name = "SmokeTest",
    size = "small",
    runtime_deps = [
        ":bazel",
    ],
)
