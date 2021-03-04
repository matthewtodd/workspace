package org.matthewtodd.wake.idea.projectImport

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor

class WakeProjectOpenProcessor : ProjectOpenProcessor() {
  override fun getName(): String = "Wake"

  override fun canOpenProject(file: VirtualFile): Boolean = false

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceNewFrame: Boolean): Project? = null
}
