# vim: ft=ruby

# https://kotlinlang.org/docs/reference/compiler-reference.html
http_file(
  name: "dist",
  url: "https://github.com/JetBrains/kotlin/releases/download/v1.3.61/kotlin-compiler-1.3.61.zip",
  sha256: "3901151ad5d94798a268d1771c6c0b7e305a608c2889fc98a674802500597b1c",
)

extract(
  name: "lib_kotlin_preloader",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-preloader.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_compiler",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-compiler.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_test",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-test.jar"],
  strip_components: 1,
)

extract(
  name: "lib_kotlin_test_junit5",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-test-junit5.jar"],
  strip_components: 1,
)

extract(
  name: "runtime",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: [
    "lib/annotations-13.0.jar",
    "lib/kotlin-reflect.jar",
    "lib/kotlin-script-runtime.jar",
    "lib/kotlin-stdlib.jar",
    "lib/trove4j.jar",
  ],
  strip_components: 1,
)

java_import(
  name: "preloader",
  jar: "//lib/kotlin:lib_kotlin_preloader",
  source_jar: "//lib/kotlin:lib_kotlin_preloader", # FIXME what if there's no source_jar?
)

java_import(
  name: "compiler",
  jar: "//lib/kotlin:lib_kotlin_compiler",
  source_jar: "//lib/kotlin:lib_kotlin_compiler", # FIXME what if there's no source_jar?
)

java_import(
  name: "test",
  jar: "//lib/kotlin:lib_kotlin_test",
  source_jar: "//lib/kotlin:lib_kotlin_test", # FIXME what if there's no source_jar?
)

java_import(
  name: "test_junit5",
  jar: "//lib/kotlin:lib_kotlin_test_junit5",
  source_jar: "//lib/kotlin:lib_kotlin_test_junit5", # FIXME what if there's no source_jar?
)
