package test.util

import play.api.mvc.{ Action, Handler, Results }
import play.api.test
import play.api.test.Helpers._

trait ConfigurableFakeApp {

  def routes: PartialFunction[Tuple2[String, String], Handler ] = {
    case _ => Action { Results.NotFound }
  }

  def configuration: Map[String, _] = Map()
  

  def fakeApplication: test.FakeApplication = test.FakeApplication(
    additionalConfiguration = configuration, withRoutes = routes) 
}