# vim: ft=ruby

ruby_lib(
  name: "concurrent",
  srcs: ["concurrent.rb"],
)

ruby_lib(
  name: "filesystem",
  srcs: ["filesystem.rb"],
)

ruby_lib(
  name: "wake",
  srcs: [
    "wake.rb",
    "wake/actions.rb",
    "wake/label.rb",
    "wake/rules.rb",
    "wake/workspace.rb",
  ],
  deps: [
    "//src/main/ruby:concurrent",
    "//src/main/ruby:filesystem",
    "//src/main/ruby/wake:testing",
  ],
)
