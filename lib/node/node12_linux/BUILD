# vim: ft=ruby

# https://nodejs.org/en/download/
http_file(
  name: "dist",
  url: "https://nodejs.org/dist/v12.18.3/node-v12.18.3-linux-x64.tar.gz",
  sha256: "8cdacecc43c35bcfa5474c793b9e7a01835e4171264f7b13f3e57093371872e9",
)

extract(
  name: "bin_node",
  archive: ":dist",
  format: :tar_gz,
  paths: ["bin/node"],
  permissions: 0755,
  strip_components: 1,
)

node_runtime(
  name: "runtime",
  node: ":bin_node",
)
