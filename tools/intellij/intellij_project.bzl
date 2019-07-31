def _app_path(filegroup):
    file_path = filegroup.files.to_list()[0].path
    (app_path, extension, discarded) = file_path.partition(".app")
    return "".join([app_path, extension])

def _dirname(path):
    return path[0:path.rindex("/")]

# TODO refactor so we can clear (and tail?) log files on launch.
def _intellij_project_impl(ctx):
    launcher = ctx.actions.declare_file(ctx.attr.name + "/launcher")
    properties = ctx.actions.declare_file("idea.properties", sibling = launcher)

    # make temp file
    ctx.actions.write(
        launcher,
        "\n".join([
            "#!/bin/bash -e",
            "",
            "# export IDEA_JDK=",
            "# export IDEA_LAUNCHER_DEBUG=true",
            "export IDEA_PROPERTIES=$(mktemp -t idea.properties)",
            "",
            "sed \\",
            "  -e s,%workspace%,$BUILD_WORKSPACE_DIRECTORY,g \\",
            "  -e s,%package%,{},g \\".format(ctx.label.package),
            "  -e s,%name%,{},g \\".format(ctx.label.name),
            "  -e s,%runfiles%,$PWD,g \\",
            "  -e s,%tempdir%,$(mktemp -d -t idea),g \\",
            "  {} > $IDEA_PROPERTIES".format(properties.short_path),
            "",
            "export PROJECT_DIR=$(mktemp -d -t project)",
            "mkdir -p $PROJECT_DIR/.idea",
            "",
            "open -a $PWD/{} $PROJECT_DIR".format(_app_path(ctx.attr.ide)),
        ]),
        True,
    )

    plugins = []
    for source in ctx.files.plugins:
        target = ctx.actions.declare_file("plugins/" + source.basename, sibling = launcher)
        ctx.actions.run_shell(
            inputs = [source],
            outputs = [target],
            command = "cp {} {}".format(source.path, target.path),
        )
        plugins.append(target)

    ctx.actions.write(
        properties,
        "\n".join([
            "bazel.project.name=%name%",
            "bazel.project.label=//%package%:%name%",
            "bazel.project.path=%workspace%",
            "idea.config.path=%workspace%/tools/intellij/config",
            "idea.is.internal=true",
            "idea.log.path=%workspace%/tools/intellij/logs/%name%",
            "idea.plugins.path=%runfiles%/{}".format(_dirname(plugins[0].short_path)),
            "idea.system.path=%tempdir%/system",
        ]),
    )

    return [
        DefaultInfo(
            executable = launcher,
            runfiles = ctx.runfiles(ctx.files.ide + [properties] + plugins),
        ),
    ]

intellij_project = rule(
    implementation = _intellij_project_impl,
    attrs = {
        "ide": attr.label(mandatory = True),
        "plugins": attr.label_list(default = []),
    },
    executable = True,
)
