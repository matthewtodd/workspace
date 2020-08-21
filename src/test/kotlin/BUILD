# vim: ft=ruby

kt_js_lib(
  name: "example-test-js",
  srcs: ["ExampleTest.kt"],
  deps: [
    "//lib/kotlin:test_js",
  ],
)

kt_js_test(
  name: "ExampleTestJs",
  runtime_deps: [":example-test-js"],
  test_module: "libexample-test-js",
)

kt_jvm_lib(
  name: "example-test-jvm",
  srcs: ["ExampleTest.kt"],
  deps: [
    "//lib/kotlin:test",
    "//lib/kotlin:test_junit5",
    "//lib/maven/org_junit_jupiter/junit_jupiter_api:jar",
  ],
)

kt_jvm_test(
  name: "ExampleTestJvm",
  runtime_deps: [":example-test-jvm"],
  test_class: "ExampleTest",
)
