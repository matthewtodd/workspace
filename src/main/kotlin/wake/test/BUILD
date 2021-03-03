# vim: ft=ruby

kt_jvm_lib(
  name: "plugin",
  srcs: ["plugin.kt"],
  deps: ["//lib/maven/org_jetbrains_kotlin/kotlin_compiler:jar"],
  resources: ["META-INF/services/org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar"],
)

kt_js_lib(
  name: "api_js",
  srcs: ["api.kt"],
  deps: [],
)

kt_js_lib(
  name: "runtime_js",
  srcs: [
    "runtime.kt",
    "runtime/js.kt",
  ],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_js:klib",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_js:klib",
  ],
  plugins: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_serialization_unshaded:jar",
  ],
)

kt_jvm_lib(
  name: "api_jvm",
  srcs: ["api.kt"],
  deps: [],
)

kt_jvm_lib(
  name: "runtime_jvm",
  srcs: [
    "runtime.kt",
    "runtime/jvm.kt",
  ],
  deps: [
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_core_jvm:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_serialization_json_jvm:jar",
  ],
  plugins: [
    "//lib/maven/org_jetbrains_kotlin/kotlin_serialization_unshaded:jar",
  ],
)

kt_native_macos_lib(
  name: "api_native",
  srcs: ["api.kt"],
  deps: [],
  os: :macos,
)

kt_native_macos_lib(
  name: "runtime_native",
  srcs: [
    "runtime.kt",
    "runtime/native.kt",
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
