# vim: ft=ruby

# https://github.com/JetBrains/kotlin/releases/latest
http_file(
  name: "dist",
  url: "https://github.com/JetBrains/kotlin/releases/download/v1.4.10/kotlin-native-macos-1.4.10.tar.gz",
  sha256: nil,
  os: :macos,
)

extract(
  name: "konan_lib",
  archive: ":dist",
  format: :tar_gz,
  paths: [
    "konan/lib/kotlin-native.jar",
    "konan/lib/trove4j.jar",
  ],
  strip_components: 1,
  os: :macos,
)

extract(
  name: "konan_konan_properties",
  archive: ":dist",
  format: :tar_gz,
  paths: ["konan/konan.properties"],
  strip_components: 1,
  suffix: ".orig",
  os: :macos,
)

kotlin_native_compiler(
  name: "compiler",
  classpath: ":konan_lib",
  properties: ":konan_konan_properties",
  os: :macos,
)
