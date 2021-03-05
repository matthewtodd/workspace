package org.matthewtodd.wake.idea

import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.matthewtodd.wake.test.Test
import org.matthewtodd.wake.test.assertThat

class WakeProjectOpenProcessorTest {
  @Test fun successful() {
    IdeaTestFixtureFactory.getFixtureFactory()
    assertThat(true).isTrue()
  }
}
