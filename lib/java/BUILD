# vim: ft=ruby

# https://jdk.java.net
toolchain_type(name: "archiver", providers: [JavaArchiverInfo])
toolchain_type(name: "runtime", providers: [JavaRuntimeInfo])

toolchain(
  name: "jdk15_macos_archiver",
  os: :macos,
  toolchain: "//lib/java/jdk15_macos:archiver",
  toolchain_type: "//lib/java:archiver",
)

toolchain(
  name: "jdk15_macos_runtime",
  os: :macos,
  toolchain: "//lib/java/jdk15_macos:runtime",
  toolchain_type: "//lib/java:runtime",
)

toolchain(
  name: "jdk15_linux_archiver",
  os: :linux,
  toolchain: "//lib/java/jdk15_linux:archiver",
  toolchain_type: "//lib/java:archiver",
)

toolchain(
  name: "jdk15_linux_runtime",
  os: :linux,
  toolchain: "//lib/java/jdk15_linux:runtime",
  toolchain_type: "//lib/java:runtime",
)
