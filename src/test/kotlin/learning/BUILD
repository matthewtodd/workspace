# vim: ft=ruby

kt_jvm_test(
  name: "CoroutinesTestJvm",
  srcs: ["CoroutinesTest.kt"],
  deps: [
    "//lib/maven/app_cash_turbine/turbine_jvm:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_coroutines_core_jvm:jar",
    "//lib/maven/org_jetbrains_kotlinx/kotlinx_coroutines_test:jar",
    "//src/main/kotlin/wake/test:api_jvm",
    "//src/main/kotlin/wake/test:runtime_jvm",
  ],
  plugins: ["//src/main/kotlin/wake/test:plugin"],
)
