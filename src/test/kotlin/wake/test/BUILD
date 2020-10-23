# vim: ft=ruby

kt_js_test(
  name: "test-result-test",
  srcs: ["TestResultTest.kt"],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:klib",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_js:klib",
    "//src/main/kotlin/wake/test:js",
  ],
)

kt_jvm_test(
  name: "TestResultTestJvm",
  srcs: ["TestResultTest.kt"],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_jvm:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_jvm:jar",
    "//src/main/kotlin/wake/test:junit",
  ],
)

kt_native_macos_test(
  name: "TestResultTestNativeMacos",
  srcs: ["TestResultTest.kt"],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_macosx64:klib",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_macosx64:klib",
    "//src/main/kotlin/wake/test:native",
  ],
  os: :macos,
)
