# vim: ft=ruby

kt_js_test(
  name: "ExampleTestJs",
  srcs: ["ExampleTest.kt"],
)

kt_jvm_test(
  name: "ExampleTestJvm",
  srcs: ["ExampleTest.kt"],
  deps: ["//src/main/kotlin/wake/test:api_jvm", "//src/main/kotlin/wake/test:runtime_jvm"],
  plugins: ["//src/main/kotlin/wake/test:plugin"],
)

kt_native_macos_test(
  name: "ExampleTestNativeMacos",
  srcs: ["ExampleTest.kt"],
  deps: ["//src/main/kotlin/wake/test:api_native", "//src/main/kotlin/wake/test:runtime_native"],
  plugins: ["//src/main/kotlin/wake/test:plugin"],
)
