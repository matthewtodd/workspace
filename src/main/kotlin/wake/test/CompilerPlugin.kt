package org.matthewtodd.wake.test

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

class WakeTestComponentRegistrar : ComponentRegistrar {
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(project, Extension())
  }
}

private class Extension : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.acceptVoid(Visitor(Generator(moduleFragment, pluginContext)))
  }
}

private class Visitor(val generator: Generator) : IrElementVisitorVoid {
  override fun visitElement(element: IrElement) {
    element.acceptChildrenVoid(this)
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction) {
    if (declaration.hasAnnotation("wake.test.Test")) {
      generator.generateTestCall(declaration)
    }
  }
}

fun IrAnnotationContainer.hasAnnotation(fqName: String) =
  annotations.any { it.symbol.owner.fqNameWhenAvailable?.parent()?.asString() == fqName }

private class Generator(val moduleFragment: IrModuleFragment, val pluginContext: IrPluginContext) {
  fun generateTestCall(@Suppress("UNUSED_PARAMETER") declaration: IrSimpleFunction) {
    throw Exception(moduleFragment.name.asString())
  }
}

// Look at kotlin's TestGenerator for overall structure:
// compiler/ir/backend.js/src/org/jetbrains/kotlin/ir/backend/js/lower/TestGenerator.kt

// Other than looping, will be
// - Making synthetic files (or singular?) with the calls
// - Running each test as in generateCodeForTestMethod

// Presumably I can look over / visit all the methods in these files, collecting them if they're annotated @Test.

// From there, how / where do I generate the new code I want to run?
// The idea is to make a method that calls all the test methods:
//
//     fun foo() {
//       test("org.matthewtodd.ExampleTest", "successful", ExampleTest()::successful)
//       test("org.matthewtodd.ExampleTest", "alsoSuccessful", ExampleTest()::alsoSuccessful)
//     }