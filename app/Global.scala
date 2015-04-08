

import scala.concurrent.duration.DurationInt

import job.SimpleJob
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
    Akka.system.scheduler.schedule(0 seconds, 60 seconds) {
      Logger.info("Running the job")
      SimpleJob.execute
    }
  }

}