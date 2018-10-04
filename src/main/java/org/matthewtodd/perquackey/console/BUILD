java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/perquackey",
    "//src/main/java/org/matthewtodd/workflow",
    "@com_github_akarnokd_rxjava2_extensions//jar",
    "@io_reactivex_rxjava2_rxjava//jar",
    "@org_reactivestreams_reactive_streams//jar",
  ],
)

java_binary(
  name = "perquackey",
  runtime_deps = [
    ":console",
  ],
  main_class = "org.matthewtodd.perquackey.console.Console",
)
