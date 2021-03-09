package org.matthewtodd.wake.idea.projectImport

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor

// I had hoped!
// But see notes in WakeProjectOpenProcessorTest.
// Until IntelliJ changes out its Project model, we're kind of stuck.
class WakeProjectOpenProcessor : ProjectOpenProcessor() {
  override fun getName(): String = "Wake"

  override fun canOpenProject(file: VirtualFile): Boolean = false

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceNewFrame: Boolean): Project? = null
}
