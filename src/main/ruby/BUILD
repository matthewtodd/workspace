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
    "//src/main/ruby:wake_testing",
  ],
)

# TODO want to move this to //src/main/ruby/wake:testing but first need to change ruby include path handling
# Actually makes it simpler, just everybody get -Isrc/main/ruby, done!
ruby_lib(
  name: "wake_testing",
  srcs: ["wake/testing.rb"],
)
