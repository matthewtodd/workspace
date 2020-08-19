# vim: ft=ruby

toolchain_type(name: "runtime", providers: [NodeRuntimeInfo])

toolchain(
  name: "node12_macos_runtime",
  os: :macos,
  toolchain: "//lib/node/node12_macos:runtime",
  toolchain_type: "//lib/node:runtime",
)

toolchain(
  name: "node12_linux_runtime",
  os: :linux,
  toolchain: "//lib/node/node12_linux:runtime",
  toolchain_type: "//lib/node:runtime",
)
