package test.util

import org.scalatest.{ Suite, SuiteMixin, TestData }
import play.api.Application
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceableModule }
import play.api.test.Helpers

/**
 * @author fbenjamin
 * Executes a single at time, but if it fails it will stop executing other tests.
 * Mix'in this trait will cause your tests to run slower!!
 *
 */
trait KontrollettiOneAppPerTestWithOverrides extends SuiteMixin { this: Suite ⇒
  def overrideModules: Seq[GuiceableModule] = Nil

  def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .overrides(overrideModules: _*)
      .build

  private var appPerTest: Application = _
  implicit final def app: Application = synchronized { appPerTest }

  abstract override def withFixture(test: NoArgTest) = {
    synchronized { appPerTest = newAppForTest(test) }
    Helpers.running(app) {
      super.withFixture(test)
    }
  }
}