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
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
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
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
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

    @Suppress("UNUSED_VARIABLE")
    val functionWakeTest = pluginContext.referenceFunctions(FqName("org.matthewtodd.wake.test.runtime.test")).single()

    // private fun IrSimpleFunctionSymbol.createInvocation(
    //     name: String,
    //     parentFunction: IrSimpleFunction,
    //     ignored: Boolean = false
    // ): FunctionWithBody {
    //     val body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, emptyList())

    //     val function = context.irFactory.buildFun {
    //         this.name = Name.identifier("$name test fun")
    //         this.returnType = context.irBuiltIns.anyNType
    //         this.origin = JsIrBuilder.SYNTHESIZED_DECLARATION
    //     }
    //     function.parent = parentFunction
    //     function.body = body

    //     val parentBody = parentFunction.body as IrBlockBody
    //     parentBody.statements += JsIrBuilder.buildCall(this).apply {
    //         putValueArgument(0, JsIrBuilder.buildString(context.irBuiltIns.stringType, name))
    //         putValueArgument(1, JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, ignored))

    //         val refType = IrSimpleTypeImpl(context.ir.symbols.functionN(0), false, emptyList(), emptyList())
    //         putValueArgument(2, JsIrBuilder.buildFunctionExpression(refType, function))
    //     }

    //     return FunctionWithBody(function, body)
    // }

    pluginContext.irFactory.addFunction(file) {
      name = Name.identifier("main")
      returnType = pluginContext.irBuiltIns.unitType
      origin = SYNTHETIC_DECLARATION
    }.also { function ->
      function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
        moduleFragment.accept(TestMethodFinder) { testClass, testMethod ->
          println(testMethod.dump())

          @Suppress("UNUSED_VARIABLE")
          val ctor = testClass.declarations.filterIsInstance<IrConstructor>().single { it.valueParameters.isEmpty() }

          @Suppress("UNUSED_VARIABLE")
          val test = org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder().also { b -> b.classifier = pluginContext.irBuiltIns.function(0) }.buildSimpleType(),
            function = pluginContext.irFactory.buildFun {
              origin = org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
              name = org.jetbrains.kotlin.name.SpecialNames.ANONYMOUS_FUNCTION
              returnType = pluginContext.irBuiltIns.unitType
            }.also { f -> f.parent = function },
            origin = org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.LAMBDA
          )

          +irCall(functionWakeTest).also { call ->
            call.putValueArgument(0, irString(testClass.name.asString()))
            call.putValueArgument(1, irString(testMethod.name.asString()))
            call.putValueArgument(2, test)
          }
        }
      }
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
