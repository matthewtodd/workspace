java_library(
    name = "terminal",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/org/matthewtodd/perquackey/terminal:__pkg__",
        "//src/test/java/org/matthewtodd/perquackey/terminal:__pkg__",
        "//src/test/java/org/matthewtodd/terminal:__pkg__",
    ],
    exports = [
        "//src/main/java/org/matthewtodd/workflow",
        "@maven//:com_googlecode_lanterna_lanterna",
    ],
    deps = [
        "//src/main/java/org/matthewtodd/flow",
        "//src/main/java/org/matthewtodd/workflow",
        "@maven//:com_googlecode_lanterna_lanterna",
    ],
)
