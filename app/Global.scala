

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.google.inject.Guice

import job.SimpleJobDispatcher
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

object Global extends GlobalSettings {
  private val logger: Logger = Logger(this.getClass())
  private lazy val simpleJob: SimpleJobDispatcher = {
    injector.getInstance(classOf[SimpleJobDispatcher])
  }

  /** binds types for Guice (Dependency Injection)**/
  private lazy val injector = {
    Play.isProd match {
      case true  => Guice.createInjector(new Production)
      case false => Guice.createInjector(new Develop)
    }
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  
  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = {
     Future.successful(InternalServerError(views.html.errors.onError(throwable)))
  }
  
// 404 - page not found error
  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
   Future.successful(NotFound(views.html.errors.onHandlerNotFound(request)))
  }
  override def onStart(app: Application) {
    logger.info("############# Application has started!")
    startJob
  }

  override def onStop(app: Application) {
    logger.info("############# Application is shutting down!")
  }

  def startJob() = {
    Akka.system.scheduler.schedule(0 minutes, 120 seconds) {
      logger.info("Started the job for synchronizing Applications with KIO")
      simpleJob.synchronizeApps 
    }
  }

}