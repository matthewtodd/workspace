load("@bazel_tools//tools/build_defs/repo:utils.bzl", "patch", "update_attrs", "workspace_and_buildfile")

def http_dmg_impl(repository_ctx):
    download_info = repository_ctx.download(
        [repository_ctx.attr.url],
        "image.dmg",
        repository_ctx.attr.sha256,
    )

    repository_ctx.execute([
        "hdiutil",
        "attach",
        "-nobrowse",
        "-readonly",
        "-noidme",
        "-mountpoint",
        "mount",
        "image.dmg",
    ])

    repository_ctx.execute(["rsync", "--archive", "mount/", "."])
    repository_ctx.execute(["hdiutil", "detach", "mount"])
    repository_ctx.execute(["rm", "image.dmg"])

    patch(repository_ctx)
    workspace_and_buildfile(repository_ctx)

    return update_attrs(repository_ctx.attr, _http_dmg_attrs.keys(), {"sha256": download_info.sha256})

_http_dmg_attrs = {
    "build_file": attr.label(allow_single_file = True),
    "build_file_content": attr.string(),
    "patch_args": attr.string_list(default = ["-p0"]),
    "patch_cmds": attr.string_list(default = []),
    "patch_tool": attr.string(default = "patch"),
    "patches": attr.label_list(default = []),
    "sha256": attr.string(),
    "strip_prefix": attr.string(),
    "url": attr.string(),
    "workspace_file": attr.label(allow_single_file = True),
    "workspace_file_content": attr.string(),
}

http_dmg = repository_rule(
    implementation = http_dmg_impl,
    attrs = _http_dmg_attrs,
)
