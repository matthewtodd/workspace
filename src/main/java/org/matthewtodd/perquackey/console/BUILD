java_library(
    name = "console",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/test/java/org/matthewtodd/perquackey/console:__pkg__",
    ],
    deps = [
        "//src/main/java/org/matthewtodd/flow",
        "//src/main/java/org/matthewtodd/perquackey",
        "//src/main/java/org/matthewtodd/workflow",
        "@com_github_akarnokd_rxjava2_extensions",
        "@com_googlecode_lanterna_lanterna",
        "@io_reactivex_rxjava2_rxjava",
    ],
)

java_binary(
    name = "perquackey",
    main_class = "org.matthewtodd.perquackey.console.Perquackey",
    runtime_deps = [
        ":console",
    ],
)
