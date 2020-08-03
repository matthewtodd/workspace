# vim: ft=ruby

toolchain_type(name: "compiler", providers: [JavaCompilerInfo])
toolchain_type(name: "runtime")

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
