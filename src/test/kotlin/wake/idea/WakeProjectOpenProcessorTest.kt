package org.matthewtodd.wake.idea

import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.matthewtodd.wake.test.Test

class WakeProjectOpenProcessorTest {
  // Can we do what we want to do yet?
  //
  // Fundamental mismatch:
  // https://github.com/bazelbuild/intellij/issues/179
  //
  // But new workspace model coming!
  // WorkspaceEntityStorage:
  // https://blog.jetbrains.com/platform/2020/10/new-implementation-of-project-model-interfaces-in-2020-3/
  //
  // AFAICT, not ready for public use, and it's not clear to me how I'd make
  // anything other than old-style Projects and Modules with it anyway.

  @Test fun successful() {
    val factory = IdeaTestFixtureFactory.getFixtureFactory()
    val fixture = factory.createBareFixture()
    fixture.getTestRootDisposable().dispose()
  }
}
