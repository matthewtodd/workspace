# vim: ft=ruby

# https://nodejs.org/en/download/
http_file(
  name: "dist",
  url: "https://nodejs.org/dist/v14.16.0/node-v14.16.0-darwin-x64.tar.gz",
  sha256: "af376caf114bdd5d7e566dbf7590e9077ffc01f9b2692eb2651f31d7219a30bb",
)

extract(
  name: "bin_node",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/node"],
  strip_components: 1,
)

node_runtime(
  name: "runtime",
  node: ":bin_node",
)
