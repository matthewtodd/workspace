# vim: ft=ruby

kt_js_test(
  name: "ExampleWakeTestJs",
  srcs: "ExampleWakeTest.kt",
)

kt_jvm_test(
  name: "ExampleTestJvm",
  srcs: ["ExampleTest.kt"],
)

kt_native_macos_test(
  name: "ExampleTestNativeMacos",
  srcs: ["ExampleTest.kt"],
  os: :macos,
)

# kt_native_macos_wake_test(
#   name: "ExampleWakeTestNativeMacos",
#   srcs: ["ExampleWakeTest.kt"],
#   deps: ["//src/main/kotlin/wake/test:api_native", "//src/main/kotlin/wake/test:runtime_native"],
#   plugins: ["//src/main/kotlin/wake/test:plugin"],
#   os: :macos,
    # verbose: true,
# )
