kt_jvm_lib(
  name: "lib",
  srcs: ["ExampleTest.kt"],
)

# TODO there's a lot of duplication here, but I'd like to keep the rules
# simpler. Perhaps macros would make sense? kt_jvm_test would expand to
# kt_jvm_lib + java_test.
kt_jvm_test(
  name: "ExampleTest",
  runtime_deps: ["//src/test/kotlin:lib"],
  test_class: "ExampleTest",
)
