# vim: ft=ruby

# https://kotlinlang.org/releases.html
# https://github.com/JetBrains/kotlin/releases/latest
# https://kotlinlang.org/docs/reference/compiler-reference.html
toolchain_type(name: "native", providers: [KotlinNativeCompilerInfo])

http_file(
  name: "dist",
  url: "https://github.com/JetBrains/kotlin/releases/download/v1.4.10/kotlin-compiler-1.4.10.zip",
  sha256: nil,
)

# The kt_*_foo rules won't use these executables; they'll instead build up the
# java command themselves. But it's nice to have them on hand for manually
# poking around, looking at command-line options, etc.
extract(
  name: "bin",
  archive: ":dist",
  format: :zip,
  paths: [
    "bin/kotlinc",
    "bin/kotlinc-js",
  ],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_compiler",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-compiler.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_preloader",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-preloader.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_stdlib",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-stdlib.jar"],
  strip_components: 1,
)
extract(
  name: "lib_kotlin_stdlib_js",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-stdlib-js.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_test",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-test.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_test_js",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-test-js.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_test_junit5",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlin-test-junit5.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlinx_serialization_compiler_plugin",
  archive: ":dist",
  format: :zip,
  paths: ["lib/kotlinx-serialization-compiler-plugin.jar"],
  strip_components: 1,
)

extract(
  name: "runtime",
  archive: ":dist",
  format: :zip,
  paths: [
    "lib/annotations-13.0.jar",
    "lib/kotlin-reflect.jar",
    "lib/kotlin-script-runtime.jar",
    "lib/trove4j.jar",
  ],
  strip_components: 1,
)

kotlin_compiler(
  name: "compiler",
  preloader: ":lib_kotlin_preloader",
  compiler: ":lib_kotlin_compiler",
  stdlib: ":stdlib",
  stdlib_js: ":stdlib_js",
  runtime: ":runtime",
  native: ":native",
)

java_import(
  name: "serialization_compiler_plugin",
  jar: ":lib_kotlinx_serialization_compiler_plugin",
)

java_import(
  name: "stdlib",
  jar: ":lib_kotlin_stdlib",
)

kt_js_import(
  name: "stdlib_js",
  jar: ":lib_kotlin_stdlib_js",
  js_path: "kotlin.js",
)

java_import(
  name: "test",
  jar: ":lib_kotlin_test",
)

kt_js_import(
  name: "test_js",
  jar: ":lib_kotlin_test_js",
  js_path: "kotlin-test.js",
)

java_import(
  name: "test_junit5",
  jar: ":lib_kotlin_test_junit5",
  neverlink: true,
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
