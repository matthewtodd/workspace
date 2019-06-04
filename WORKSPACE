load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load("//tools:http_dmg.bzl", "http_dmg")

# Check for updates at https://www.jetbrains.com/idea/download/download-thanks.html
http_dmg(
    name = "intellij",
    build_file_content = "\n".join([
        "filegroup(",
        "    name = 'app',",
        "    srcs = glob(['IntelliJ.app/**/*']),",
        "    visibility = ['//visibility:public']",
        ")",
        "",
        "java_import(",
        "    name = 'sdk',",
        "    jars = glob(['IntelliJ.app/Contents/lib/*.jar']),",
        "    srcjar = '@intellij_sdk_sources//jar',",
        "    visibility = ['//visibility:public']",
        ")",
    ]),
    patch_cmds = [
        "rm Applications",
        "mv 'IntelliJ IDEA CE.app' IntelliJ.app",
    ],
    sha256 = "3ecfe9d52e4c02e68389ac1a0087c526b6f89a4048d3f49048b2bff1728a26b3",
    url = "https://download.jetbrains.com/idea/ideaIC-2019.1.3.dmg",
)

# Check for updates at https://www.jetbrains.com/intellij-repository/releases
http_jar(
    name = "intellij_sdk_sources",
    sha256 = "0c497130ee855f75951acfc8ddbbe7a069f53c6f36f9e296c71177f5e698de63",
    url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/2019.1.3/ideaIC-2019.1.3-sources.jar",
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
