load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "1.1"
RULES_JVM_EXTERNAL_SHA = "ade316ec98ba0769bb1189b345d9877de99dd1b1e82f5a649d6ccbcb8da51c1f"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    # Check for updates by pasting coordinates at https://search.maven.org
    artifacts = [
        "com.github.akarnokd:rxjava2-extensions:0.20.8",
        "com.googlecode.lanterna:lanterna:3.0.1",
        "io.reactivex.rxjava2:rxjava:2.2.7",
        "junit:junit:4.12",
        "org.assertj:assertj-core:3.12.2",
        "org.reactivestreams:reactive-streams:1.0.2",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    fetch_sources = True,
)
