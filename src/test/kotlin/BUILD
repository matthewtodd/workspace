# vim: ft=ruby

kt_js_lib(
  name: "js",
  srcs: ["ExampleTest.kt"],
  deps: [
    "//lib/kotlin:test_js",
  ],
)

kt_js_test(
  name: "ExampleTestJs",
)

kt_jvm_lib(
  name: "jvm",
  srcs: ["ExampleTest.kt"],
  deps: [
    "//lib/kotlin:test",
    "//lib/kotlin:test_junit5",
    "//lib/maven/org_junit_jupiter/junit_jupiter_api:jar",
  ],
)

kt_jvm_test(
  name: "ExampleTestJvm",
  runtime_deps: ["//src/test/kotlin:jvm"],
  test_class: "ExampleTest",
)
