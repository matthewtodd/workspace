# vim: ft=ruby

# https://jdk.java.net
toolchain_type(name: "archiver", providers: [JavaArchiverInfo])
toolchain_type(name: "compiler", providers: [JavaCompilerInfo])
toolchain_type(name: "runtime", providers: [JavaRuntimeInfo])

toolchain(
  name: "jdk14_macos_archiver",
  os: :macos,
  toolchain: "//lib/java/jdk14_macos:archiver",
  toolchain_type: "//lib/java:archiver",
)

toolchain(
  name: "jdk14_macos_compiler",
  os: :macos, # NOTE maybe we don't need to build constraints yet
  toolchain: "//lib/java/jdk14_macos:compiler", # relative labels?
  toolchain_type: "//lib/java:compiler",
)

toolchain(
  name: "jdk14_macos_runtime",
  os: :macos,
  toolchain: "//lib/java/jdk14_macos:runtime",
  toolchain_type: "//lib/java:runtime",
)

toolchain(
  name: "jdk14_linux_archiver",
  os: :linux,
  toolchain: "//lib/java/jdk14_linux:archiver",
  toolchain_type: "//lib/java:archiver",
)

toolchain(
  name: "jdk14_linux_compiler",
  os: :linux,
  toolchain: "//lib/java/jdk14_linux:compiler",
  toolchain_type: "//lib/java:compiler",
)

toolchain(
  name: "jdk14_linux_runtime",
  os: :linux,
  toolchain: "//lib/java/jdk14_linux:runtime",
  toolchain_type: "//lib/java:runtime",
)
