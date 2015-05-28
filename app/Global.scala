

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

object Global extends GlobalSettings {
  private val logger: Logger = Logger(this.getClass())
  private lazy val simpleJob: SimpleJob = {
    injector.getInstance(classOf[SimpleJob])
  }

  /** bind types for Guice (Dependency Injection)**/
  private lazy val injector = {
    Play.isProd match {
      case true  => Guice.createInjector(new Production)
      case false => Guice.createInjector(new Develop)
    }
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  override def onStart(app: Application) {
    logger.info("Application has started")
    startJob
  }

  override def onStop(app: Application) {
    logger.info("Application shutdown...")
  }

  def startJob() = {
    Akka.system.scheduler.schedule(0 minutes, 5 minutes) {
      logger.info("Started the job")
      simpleJob.execute 
    }
  }

}