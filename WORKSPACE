load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# rules_xcodeproj
# WORKSPACE snippets can be copied and pasted from the release notes:
# https://github.com/buildbuddy-io/rules_xcodeproj/releases

http_archive(
    name = "com_github_buildbuddy_io_rules_xcodeproj",
    sha256 = "564381b33261ba29e3c8f505de82fc398452700b605d785ce3e4b9dd6c73b623",
    url = "https://github.com/buildbuddy-io/rules_xcodeproj/releases/download/0.9.0/release.tar.gz",
)

load(
    "@com_github_buildbuddy_io_rules_xcodeproj//xcodeproj:repositories.bzl",
    "xcodeproj_rules_dependencies",
)

xcodeproj_rules_dependencies()

load(
    "@build_bazel_rules_apple//apple:repositories.bzl",
    "apple_rules_dependencies",
)

apple_rules_dependencies()

load(
    "@build_bazel_rules_swift//swift:repositories.bzl",
    "swift_rules_dependencies",
)

swift_rules_dependencies()

load(
    "@build_bazel_rules_swift//swift:extras.bzl",
    "swift_rules_extra_dependencies",
)

swift_rules_extra_dependencies()

load(
    "@build_bazel_apple_support//lib:repositories.bzl",
    "apple_support_dependencies",
)

apple_support_dependencies()

# wren
# https://github.com/wren-lang/wren/releases

http_archive(
    name = "io_wren",
    sha256 = "23c0ddeb6c67a4ed9285bded49f7c91714922c2e7bb88f42428386bf1cf7b339",
    url = "https://github.com/wren-lang/wren/archive/refs/tags/0.4.0.tar.gz",
    strip_prefix = "wren-0.4.0",
    build_file_content = """
cc_library(
    name = "wren",
    srcs = glob(["src/vm/*.h", "src/vm/*.c", "src/vm/*.inc"]),
    hdrs = ["src/include/wren.h"],
    copts = ["-Iexternal/io_wren/src/include", "-Iexternal/io_wren/src/vm"],
    local_defines = ["WREN_OPT_META=0", "WREN_OPT_RANDOM=0"],
    tags = ["swift_module=Wren"],
    visibility = ["@//src:__pkg__"],
)
""",
)
