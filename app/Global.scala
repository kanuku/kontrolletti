

import scala.concurrent.duration.DurationInt

import v1.job.SimpleJob
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext



object Global extends GlobalSettings {
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