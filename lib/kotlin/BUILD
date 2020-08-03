# vim: ft=ruby

# https://kotlinlang.org/docs/reference/compiler-reference.html
http_file(
  name: "dist",
  url: "https://github.com/JetBrains/kotlin/releases/download/v1.3.61/kotlin-compiler-1.3.61.zip",
  sha256: "3901151ad5d94798a268d1771c6c0b7e305a608c2889fc98a674802500597b1c",
)

extract(
  name: "preloader",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-preloader.jar"],
  strip_components: 1,
)

extract(
  name: "compiler",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: ["lib/kotlin-compiler.jar"],
  strip_components: 1,
)

extract(
  name: "test",
  archive: "//lib/kotlin:dist",
  format: :zip,
  paths: [
    "lib/kotlin-test.jar",
    "lib/kotlin-test-junit5.jar",
  ],
  strip_components: 1,
)
