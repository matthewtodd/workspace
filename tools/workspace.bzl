load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "convert_artifact_coordinate_to_urls")

def _name_for(coordinates):
    parts = coordinates.split(":")
    group_id = parts[0]
    artifact_id = parts[1]
    return "%s_%s" % (group_id.replace(".", "_"), artifact_id.replace("-", "_"))

# Adds naming convention, default server_urls, and srcjar_urls generation to
# jvm_maven_import_external.
# Also works around a bug wherein jvm_import_external writes out the repr of
# visibility labels, so the generated external BUILD files say Label("//foo")
# instead of //foo, breaking bazel.
# TODO delegate to jvm_maven_import_external if it ever starts generating
# srcjar_urls.
# TODO do something real for licenses?
def java_import_maven_jar(artifact, visibility, **kwargs):
    server_urls = ["https://repo1.maven.org/maven2"]
    jar_urls = convert_artifact_coordinate_to_urls(artifact, server_urls, "jar")
    srcjar_urls = [url.replace(".jar", "-sources.jar") for url in jar_urls]

    java_import_external(
        name = _name_for(artifact),
        jar_urls = jar_urls,
        srcjar_urls = srcjar_urls,
        licenses = [],
        additional_rule_attrs = {"visibility": repr(visibility)},
        **kwargs
    )
