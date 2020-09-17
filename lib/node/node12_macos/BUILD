# vim: ft=ruby

# https://nodejs.org/en/download/
http_file(
  name: "dist",
  url: "https://nodejs.org/dist/v12.18.3/node-v12.18.3-darwin-x64.tar.gz",
  sha256: "af376caf114bdd5d7e566dbf7590e9077ffc01f9b2692eb2651f31d7219a30bb",
  os: :macos,
)

extract(
  name: "bin_node",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/node"],
  permissions: 0755,
  strip_components: 1,
  os: :macos,
)

node_runtime(
  name: "runtime",
  node: ":bin_node",
  os: :macos,
)
