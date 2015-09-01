

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import com.google.inject.Guice
import module.Develop
import module.Production
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.NotFound
import jobs.ImportImpl
import jobs.Import

object Global extends GlobalSettings {
  private val logger: Logger = Logger(this.getClass())
  private lazy val job = {
    injector.getInstance(classOf[ImportImpl])
  }

  /** binds types for Guice (Dependency Injection)**/
  private lazy val injector = {
    Play.isProd match {
      case true  => Guice.createInjector(new Production)
      case false => Guice.createInjector(new Develop)
    }
  }

   def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

 
}