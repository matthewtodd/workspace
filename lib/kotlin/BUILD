# vim: ft=ruby

# https://kotlinlang.org/releases.html
toolchain_type(name: "native", providers: [KotlinNativeCompilerInfo])

kotlin_compiler(
  name: "compiler",
  classpath: [
    "//lib/maven/org_jetbrains/annotations:jar",
    "//lib/maven/org_jetbrains_intellij_deps/trove4j:jar",
    "//lib/maven/org_jetbrains_kotlin/kotlin_compiler:jar",
    "//lib/maven/org_jetbrains_kotlin/kotlin_reflect:jar",
    "//lib/maven/org_jetbrains_kotlin/kotlin_script_runtime:jar",
    "//lib/maven/org_jetbrains_kotlin/kotlin_stdlib:jar",
  ],
  native: ":native",
)

toolchain(
  name: "native_linux_compiler",
  os: :linux,
  toolchain: "//lib/kotlin/native_linux:compiler",
  toolchain_type: "//lib/kotlin:native",
)

toolchain(
  name: "native_macos_compiler",
  os: :macos,
  toolchain: "//lib/kotlin/native_macos:compiler",
  toolchain_type: "//lib/kotlin:native",
)
