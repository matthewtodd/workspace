# vim: ft=ruby

ruby_test(
  name: "ConcurrentTest",
  srcs: ["concurrent_test.rb"],
  deps: [
    "//lib/rubygems/minitest:minitest",
    "//src/main/ruby/minitest:wake_plugin",
    "//src/main/ruby:concurrent",
  ],
)

ruby_test(
  name: "WakeTest",
  srcs: ["wake_test.rb"],
  deps: [
    "//lib/rubygems/minitest:minitest",
    "//src/main/ruby/minitest:wake_plugin",
    "//src/main/ruby:wake",
  ],
)
