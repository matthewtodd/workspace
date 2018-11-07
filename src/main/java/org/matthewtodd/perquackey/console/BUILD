java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/console",
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
    "@com_github_akarnokd_rxjava2_extensions//jar",
    "@io_reactivex_rxjava2_rxjava//jar",
    "@org_jline_jline_terminal//jar",
    "@org_reactivestreams_reactive_streams//jar",
  ],
  visibility = [
    "//src/test/java/org/matthewtodd/perquackey/console:__pkg__",
  ],
)

java_binary(
  name = "perquackey",
  runtime_deps = [
    ":console",
  ],
  main_class = "org.matthewtodd.perquackey.console.Console",
)
