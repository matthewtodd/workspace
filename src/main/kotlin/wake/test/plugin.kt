package org.matthewtodd.wake.test

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.SourceManager
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class WakeTestComponentRegistrar : ComponentRegistrar {
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(project, WakeTestIrGenerationExtension)
  }
}

private object WakeTestIrGenerationExtension : IrGenerationExtension {
  private val testSuites = mutableMapOf<IrModuleFragment, IrSimpleFunction>()

  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    @Suppress("UNUSED_VARIABLE")
    val wakeTestFunctionSymbol = pluginContext.referenceFunctions(FqName("org.matthewtodd.wake.test.runtime.test")).single()

    // TODO this doesn't work. Either need a stable key, or a way to look for a main method!
    testSuites.getOrPut(moduleFragment) {
      val file = IrFileImpl(
        object : SourceManager.FileEntry {
          override val name = "<test suite>"
          override val maxOffset = UNDEFINED_OFFSET

          override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
            SourceRangeInfo(
              "",
              UNDEFINED_OFFSET,
              UNDEFINED_OFFSET,
              UNDEFINED_OFFSET,
              UNDEFINED_OFFSET,
              UNDEFINED_OFFSET,
              UNDEFINED_OFFSET
            )

          override fun getLineNumber(offset: Int) = UNDEFINED_OFFSET
          override fun getColumnNumber(offset: Int) = UNDEFINED_OFFSET
        },
        EmptyPackageFragmentDescriptor(moduleFragment.descriptor, FqName.ROOT)
      )

      moduleFragment.files += file

      val mainFunction = pluginContext.irFactory.addFunction(file) {
        name = Name.identifier("main")
        returnType = pluginContext.irBuiltIns.unitType
        origin = SYNTHETIC_DECLARATION
      }

      mainFunction.body = DeclarationIrBuilder(pluginContext, mainFunction.symbol).irBlockBody {
        moduleFragment.accept(TestMethodFinder) { testClass, testMethod ->
          val constructor = testClass.declarations.filterIsInstance<IrConstructor>().single { it.valueParameters.isEmpty() }
          println(constructor.dump())
          println(testMethod.dump())

          // generate code!
          // 0. new lambda () -> Unit
          // 1. new instance of test class
          // 2. invoke test method
          // 3. find the test method in the runtime API
          // 4. invoke it with name, name, lambda
        }
      }

      mainFunction
    }
  }
}

private object SYNTHETIC_DECLARATION : IrDeclarationOriginImpl("SYNTHETIC_DECLARATION", isSynthetic = true)

typealias TestMethodConsumer = (IrClass, IrSimpleFunction) -> Unit

private object TestMethodFinder : IrElementVisitor<Unit, TestMethodConsumer> {
  override fun visitElement(element: IrElement, data: TestMethodConsumer) {
    element.acceptChildren(this, data)
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction, data: TestMethodConsumer) {
    if (declaration.hasAnnotation(FqName("org.matthewtodd.wake.test.Test"))) {
      if (declaration.parent is IrClass) {
        data(declaration.parent as IrClass, declaration)
      }
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
