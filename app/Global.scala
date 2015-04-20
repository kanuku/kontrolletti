

import scala.concurrent.duration.DurationInt
import v1.job.SimpleJob
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Guice
import play.api.Play
import v1.module.ProductionModule
import v1.module.DevelopModule

object Global extends GlobalSettings {

  /** bind types for Guice (Dependency Injection)**/
  private lazy val injector = {
    Play.isProd match {
      case true  => Guice.createInjector(new ProductionModule)
      case false => Guice.createInjector(new DevelopModule)
    }
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  override def onStart(app: Application) {
    Logger.info("Application has started")
    startJob
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  def startJob = {
    Akka.system.scheduler.schedule(0 minutes, 5 minutes) {
      Logger.info("Running the job")
      SimpleJob.execute
    }
  }

}