load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")

# Check for updates at https://www.jetbrains.com/intellij-repository/releases
http_archive(
    name = "intellij",
    build_file_content = "\n".join([
        "java_import(",
        "    name = 'sdk',",
        "    jars = glob(['lib/*.jar']),",
        "    srcjar = '@intellij_sdk_sources//jar',",
        "    visibility = ['//visibility:public'],",
        ")",
    ]),
    sha256 = "e045751adabe2837203798270e1dc173128fe3e607e3025d4f8110c7ed4cc499",
    url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/2019.1.2/ideaIC-2019.1.2.zip",
)

http_jar(
    name = "intellij_sdk_sources",
    sha256 = "a86b0af9758aa70360fd4113db878a91a10e67f6b0816aa8826ad5d9c4d17894",
    url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/2019.1.2/ideaIC-2019.1.2-sources.jar",
)

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
        "io.reactivex.rxjava2:rxjava:2.2.8",
        "junit:junit:4.12",
        "org.assertj:assertj-core:3.12.2",
        "org.reactivestreams:reactive-streams:1.0.2",
    ],
    fetch_sources = True,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)
