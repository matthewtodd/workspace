# vim: ft=ruby

ruby_lib(
  name: "wake",
  srcs: [
    "wake.rb",
    "wake/count_down_latch.rb",
    "wake/label.rb",
    "wake/rules.rb",
  ],
  deps: ["//src/main/ruby:wake_testing"],
)

# TODO want to move this to //src/main/ruby/wake:testing but first need to change ruby include path handling
# Actually makes it simpler, just everybody get -Isrc/main/ruby, done!
ruby_lib(
  name: "wake_testing",
  srcs: ["wake/testing.rb"],
)
