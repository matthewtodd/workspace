load(":intellij_project.bzl", "intellij_project")

intellij_project(
    name = "intellij",
    ide = "@intellij//:app",
    # modules
    plugins = [
        "//src/main/java/org/matthewtodd/intellij/bazel",
        "//src/main/java/org/matthewtodd/intellij/rebalance",
    ],
)
