

import scala.concurrent.duration.DurationInt
import job.SimpleJob
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Guice
import play.api.Play
import module.Production
import module.Develop
import job.SimpleJob
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {
  private val logger: Logger = Logger(this.getClass())
  private lazy val simpleJob: SimpleJob = {
    injector.getInstance(classOf[SimpleJob])
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
      logger.info("Started the job")
      simpleJob.execute 
    }
  }

}