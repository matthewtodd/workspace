# vim: ft=ruby

# https://kotlinlang.org/docs/reference/compiler-reference.html
http_file(
  name: "dist",
  url: "https://github.com/JetBrains/kotlin/releases/download/v1.4.0/kotlin-compiler-1.4.0.zip",
  sha256: "590391d13b3c65ba52cba470f56efd5b14e2b1f5b9459f63aa12eb38ef52f161",
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
)

java_import(
  name: "stdlib",
  jar: ":lib_kotlin_stdlib",
)

kt_js_import(
  name: "stdlib_js",
  jar: ":lib_kotlin_stdlib_js",
)

java_import(
  name: "test",
  jar: ":lib_kotlin_test",
)

kt_js_import(
  name: "test_js",
  jar: ":lib_kotlin_test_js",
)

java_import(
  name: "test_junit5",
  jar: ":lib_kotlin_test_junit5",
  neverlink: true,
)
