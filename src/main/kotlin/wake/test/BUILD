# vim: ft=ruby

kt_js_lib(
  name: "wake-test",
  srcs: [
    "JavascriptTestRunner.kt",
    "TestResult.kt",
  ],
  deps: [
    "//lib/kotlin:test_js",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:jar",
  ],
  plugins: [
    "//lib/kotlin:serialization_compiler_plugin",
  ],
)

kt_jvm_lib(
  name: "junit",
  srcs: [
    "JUnitLauncher.kt",
    "TestResult.kt",
  ],
  deps: [
    "//lib/maven/org_apiguardian/apiguardian_api:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_jvm:jar",
    "//lib/maven/org_junit_jupiter/junit_jupiter_engine:jar",
    "//lib/maven/org_junit_platform/junit_platform_commons:jar",
    "//lib/maven/org_junit_platform/junit_platform_engine:jar",
    "//lib/maven/org_junit_platform/junit_platform_launcher:jar",
    "//lib/maven/org_opentest4j/opentest4j:jar",
  ],
  plugins: [
    "//lib/kotlin:serialization_compiler_plugin",
  ],
  debug: true,
)
