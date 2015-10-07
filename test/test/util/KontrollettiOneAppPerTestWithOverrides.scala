package test.util

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test._
import org.scalatest._
import module.Development

/**
 * @author fbenjamin
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