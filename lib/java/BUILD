# vim: ft=ruby

toolchain_type(name: "compiler")
toolchain_type(name: "runtime")

http_archive(
  name: "jdk13_macos",
  url: "https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_osx-x64_bin.tar.gz",
  # sha256: "08fd2db3a3ab6fb82bb9091a035f9ffe8ae56c31725f4e17d573e48c39ca10dd",
  strip_components: 3,
  build: ->() {
    java_compiler(
      name: "compiler",
    )

    java_runtime(
      name: "runtime",
    )
  },
)

http_archive(
  name: "jdk13_linux",
  url: "https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz",
  # sha256: "acc7a6aabced44e62ec3b83e3b5959df2b1aa6b3d610d58ee45f0c21a7821a71",
  strip_components: 1,
  build: ->() {
    java_compiler(
      name: "compiler",
    )

    java_runtime(
      name: "runtime",
    )
  },
)

toolchain(
  name: "jdk13_macos_compiler",
  os: :macos, # NOTE maybe we don't need to build constraints yet
  toolchain: "//lib/java/jdk13_macos:compiler", # relative labels?
  toolchain_type: "//lib/java:compiler",
)

toolchain(
  name: "jdk13_macos_runtime",
  os: :macos,
  toolchain: "//lib/java/jdk13_macos:runtime",
  toolchain_type: "//lib/java:runtime",
)

toolchain(
  name: "jdk13_linux_compiler",
  os: :linux,
  toolchain: "//lib/java/jdk13_linux:compiler",
  toolchain_type: "//lib/java:compiler",
)

toolchain(
  name: "jdk13_linux_runtime",
  os: :linux,
  toolchain: "//lib/java/jdk13_linux:runtime",
  toolchain_type: "//lib/java:runtime",
)
