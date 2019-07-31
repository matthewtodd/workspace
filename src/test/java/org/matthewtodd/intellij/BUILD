java_library(
    name = "intellij",
    testonly = True,
    srcs = glob(["**/*.java"]),
    visibility = ["//src/test/java/org/matthewtodd/intellij:__subpackages__"],
    deps = [
        "//third_party:jpda-jdi",
        "@intellij//:sdk",
    ],
)
