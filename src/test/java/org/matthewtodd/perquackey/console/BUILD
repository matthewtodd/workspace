java_library(
  name = "console",
  srcs = glob(["*.java"]),
  deps = [
    "//src/main/java/org/matthewtodd/console",
    "//src/main/java/org/matthewtodd/perquackey/console",
    "//src/test/java/org/matthewtodd/perquackey",
    "//third_party:assertj",
    "//third_party:hamcrest",
    "//third_party:junit",
    "//third_party:reactive_streams",
    "//third_party:rxjava2",
  ],
)

java_test(
  name = "PerquackeyTest",
  size = "small",
  runtime_deps = [
    ":console",
  ],
)
