load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Check for updates at https://github.com/bazelbuild/rules_jvm_external/releases
http_archive(
    name = "rules_jvm_external",
    sha256 = "63a9162eb8b530da76453857bd3404db8852080976b93f237577f8000287e73d",
    strip_prefix = "rules_jvm_external-1.3",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/1.3.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    # Check for updates by pasting coordinates at https://search.maven.org
    artifacts = [
        "com.github.akarnokd:rxjava2-extensions:0.20.8",
        "com.googlecode.lanterna:lanterna:3.0.1",
        "com.jetbrains.intellij.platform:core:191.7141.44",
        "com.jetbrains.intellij.platform:editor:191.7141.44",
        "com.jetbrains.intellij.platform:ide-impl:191.7141.44",
        "com.jetbrains.intellij.platform:util:191.7141.44",
        "io.reactivex.rxjava2:rxjava:2.2.8",
        "junit:junit:4.12",
        "org.assertj:assertj-core:3.12.2",
        "org.reactivestreams:reactive-streams:1.0.2",
    ],
    fetch_sources = True,
    repositories = [
        "https://jcenter.bintray.com/",
        "https://jetbrains.bintray.com/intellij-third-party-dependencies",
        "https://repo1.maven.org/maven2",
        "https://www.jetbrains.com/intellij-repository/releases",
    ],
)
