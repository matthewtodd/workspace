java_library(
    name = "terminal",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey/terminal:__pkg__",
    ],
    deps = [
        "//src/main/java/org/matthewtodd/flow",
        "//src/main/java/org/matthewtodd/perquackey",
        "//src/main/java/org/matthewtodd/workflow",
        "@com_googlecode_lanterna_lanterna",
    ],
)

java_binary(
    name = "perquackey",
    main_class = "org.matthewtodd.perquackey.terminal.Perquackey",
    runtime_deps = [
        ":terminal",
    ],
)
