# Notes

## TODO

- Write new `kt_js_test` rule.
- Write new `kt_native_macos_test` rule.
- Write new `kt_jvm_test` rule.
- Remove old test support infrastructure.

## ABI class generation is implemented as a compiler plugin.

Command-line usage:

- Add a path to 'jvm-abi-gen.jar' to the plugin classpath argument (-Xplugin).
  By default the jar is located at 'kotlinc/lib/jvm-abi-gen.jar' in the kotlinc
  distribution archive.
- Specify an output directory for ABI classes via
  -Pplugin:org.jetbrains.kotlin.jvm.abi:outputDir=<DIR>.

Except maybe I'll use klibs as intermediate artifacts instead?

## Incremental compilation

https://kotlinlang.org/docs/reference/using-maven.html#incremental-compilation

To make your builds faster, you can enable incremental compilation for Maven
(supported since Kotlin 1.1.2). In order to do that, define the
kotlin.compiler.incremental property:

    <properties>
        kotlin.compiler.incrementaltrue</kotlin.compiler.incremental>
    </properties>

Alternatively, run your build with the -Dkotlin.compiler.incremental=true
option.

https://kotlinlang.org/docs/reference/kapt.html#incremental-annotation-processing-since-1330

Starting from version 1.3.30, kapt supports incremental annotation processing
as an experimental feature. Currently, annotation processing can be incremental
only if all annotation processors being used are incremental.

Incremental annotation processing is enabled by default starting from version
1.3.50. To disable incremental annotation processing, add this line to your
gradle.properties file:

    kapt.incremental.apt=false

Note that incremental annotation processing requires incremental compilation to
be enabled as well.
