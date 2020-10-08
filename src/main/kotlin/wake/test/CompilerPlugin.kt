package org.matthewtodd.wake.test

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class WakeTestComponentRegistrar : ComponentRegistrar {
  override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(project, WakeTestIrGenerationExtension())
  }
}

class WakeTestIrGenerationExtension : IrGenerationExtension {
  override fun generate(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext
  ) {
    for (file in moduleFragment.files) {
      println(file)
    }
  }
}
