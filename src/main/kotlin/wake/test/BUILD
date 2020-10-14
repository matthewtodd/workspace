# vim: ft=ruby

kt_jvm_lib(
  name: "plugin_raw",
  srcs: ["CompilerPlugin.kt"],
  deps: ["//lib/maven/org_jetbrains_kotlin/kotlin_compiler:jar"],
)

# Not sure this is what I want, but it at least keeps me from modifying the kt_jvm_lib rule while I'm exploring.
java_lib_with_resources(
  name: "plugin",
  jar: ":plugin_raw",
  services: {
    "org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar" => "org.matthewtodd.wake.test.WakeTestComponentRegistrar",
  },
)

kt_js_lib(
  name: "js",
  srcs: [
    "JavascriptTestRunner.kt",
    "TestResult.kt",
    "TestRunner.kt",
  ],
  deps: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_test_js:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_js:jar",
  ],
  plugins: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_serialization_unshaded:jar",
  ],
)

kt_jvm_lib(
  name: "junit",
  srcs: [
    "JUnitLauncher.kt",
    "TestResult.kt",
  ],
  deps: [
    "//lib/maven/org_apiguardian/apiguardian_api:jar", # NO
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_jvm:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_jvm:jar",
    "//lib/maven/org_junit_jupiter/junit_jupiter_engine:jar",
    "//lib/maven/org_junit_platform/junit_platform_commons:jar", # NO?
    "//lib/maven/org_junit_platform/junit_platform_engine:jar",
    "//lib/maven/org_junit_platform/junit_platform_launcher:jar",
    "//lib/maven/org_opentest4j/opentest4j:jar", # NO
  ],
  plugins: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_serialization_unshaded:jar",
  ],
  debug: true,
)

kt_native_macos_lib(
  name: "native",
  srcs: [
    "NativeTestRunner.kt",
    "TestResult.kt",
    "TestRunner.kt",
  ],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_macosx64:klib",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_macosx64:klib",
  ],
  plugins: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_serialization_unshaded:jar",
  ],
  os: :macos,
)
