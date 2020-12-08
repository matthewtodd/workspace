package org.matthewtodd.wake.test

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName

class WakeTestComponentRegistrar : ComponentRegistrar {
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(project, WakeTestIrGenerationExtension)
  }
}

private object WakeTestIrGenerationExtension : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.accept(WakeTestIrElementVisitor) { e -> println(e.dump()) }
  }
}

private object WakeTestIrElementVisitor : IrElementVisitor<Unit, (IrSimpleFunction) -> Unit> {
  override fun visitElement(element: IrElement, data: (IrSimpleFunction) -> Unit) {
    element.acceptChildren(this, data)
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction, data: (IrSimpleFunction) -> Unit) {
    if (declaration.hasAnnotation(FqName("wake.test.Test"))) {
      data(declaration)
    }
  }
}

// Look at kotlin's TestGenerator for overall structure:
// compiler/ir/backend.js/src/org/jetbrains/kotlin/ir/backend/js/lower/TestGenerator.kt

// Other than looping, will be
// - Making synthetic files (or singular?) with the calls
// - Running each test as in generateCodeForTestMethod

// From there, how / where do I generate the new code I want to run?
// The idea is to make a method that calls all the test methods:
//
//     fun foo() {
//       test("org.matthewtodd.ExampleTest", "successful", ExampleTest()::successful)
//       test("org.matthewtodd.ExampleTest", "alsoSuccessful", ExampleTest()::alsoSuccessful)
//     }

// Look at JsIrBuilder.buildFunction, in
// compiler/ir/backend.js/src/org/jetbrains/kotlin/ir/backend/js/ir/IrBuilder.kt
// Also look at how JS code calls its main function
// org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.IrModuleToJsTransformer#generateCallToMain
