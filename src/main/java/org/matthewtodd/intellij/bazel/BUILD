java_library(
    name = "bazel",
    srcs = glob(["**/*.java"]),
    resource_strip_prefix = "src/main/java/org/matthewtodd/intellij/bazel",
    resources = glob(["META-INF/*"]),
    visibility = [
        "//src/test/java/org/matthewtodd/intellij/bazel:__pkg__",
        "//tools/intellij:__pkg__",
    ],
    deps = [
        "@intellij//:sdk",
    ],
)
