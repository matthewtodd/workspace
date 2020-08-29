# vim: ft=ruby

kt_js_lib(
  name: "test-result-test-js",
  srcs: ["TestResultTest.kt"],
  deps: [
    "//lib/kotlin:test_js",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:jar",
    "//src/main/kotlin/wake/test:wake-test",
  ],
)

kt_js_test(
  name: "TestResultTestJs",
  runtime_deps: [":test-result-test-js"],
  test_module: "libtest-result-test-js",
)

kt_js_lib(
  name: "wake-test-test-js",
  srcs: ["WakeTestTest.kt"],
  deps: [
    "//lib/kotlin:test_js",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:jar",
    "//src/main/kotlin/wake/test:wake-test",
  ],
)

kt_js_test(
  name: "WakeTestTestJs",
  runtime_deps: [":wake-test-test-js"],
  test_module: "libwake-test-test-js",
)

kt_jvm_lib(
  name: "test-result-test-jvm",
  srcs: ["TestResultTest.kt"],
  deps: [
    "//lib/kotlin:test",
    "//lib/kotlin:test_junit5",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_jvm:jar",
    "//lib/maven/org_junit_jupiter/junit_jupiter_api:jar",
    "//src/main/kotlin/wake/test:junit",
  ],
)

kt_jvm_test(
  name: "TestResultTestJvm",
  runtime_deps: [":test-result-test-jvm"],
  test_class: "org.matthewtodd.wake.test.TestResultTest",
)
