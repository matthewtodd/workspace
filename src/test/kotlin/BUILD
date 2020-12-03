# vim: ft=ruby

kt_js_test(
  name: "ExampleTestJs",
  srcs: ["ExampleTest.kt"],
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

kt_js_lib(
  name: "ExampleWakeTestJs",
  srcs: "ExampleWakeTest.kt",
  deps: ["//src/main/kotlin/wake/test:api_js"],
  plugins: ["//src/main/kotlin/wake/test:plugin"],
  verbose: true,
)
