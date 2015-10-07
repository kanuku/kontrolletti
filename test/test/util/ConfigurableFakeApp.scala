package test.util

import play.api.mvc.{ Action, Handler, Results }
import play.api.test
import play.api.test.Helpers._

/**
 * A configurable FakeApplication for testing. <br>
 * Http-Filter is disabled by default.
 */
trait ConfigurableFakeApp {

  def routes: PartialFunction[Tuple2[String, String], Handler] = {
    case _ => Action { Results.NotFound }
  }

  def configuration: Map[String, _] = Map()

  def disableHttpFilters: Boolean = true

  def fakeApplication: test.FakeApplication = test.FakeApplication(
    additionalConfiguration = lodConfiguration(), withRoutes = routes)

  private def lodConfiguration(): Map[String, _] = {
    disableHttpFilters match {
      case true  => configuration ++ (Map("play.http.filters" -> null))
      case false => configuration
    }
  }
}