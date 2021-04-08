# Notes

## TODO

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

## Output caching?

As some of you might know, in 1.3.70 we added support for compiler caches for
macosX64 and iosX64 targets. Long story short, compiler caches make compilation
time of debug builds (e.g. linkDebug…) in Gradle significantly faster (well,
except the first one, when dependencies are caching and Gradle daemon is
warming up).

In 1.5.0-M1 we add opt-in support for compiler caches for two more targets:
- iosArm64
- linuxX64 (only on Linux host)

To enable them add a single line to your gradle.properties.
For linuxX64 target: kotlin.native.cacheKind.linuxX64=static
For iosArm64 target: kotlin.native.cacheKind.iosArm64=static

“Why not enable it by default?” you might ask. While it doesn’t break our test
projects, it might break yours.

And who likes broken compilation after compiler update? :)

So, we asking you to test compiler caches on your projects and report to us if
you encounter any kind of problems. Let’s make Kotlin faster together! (edited)
