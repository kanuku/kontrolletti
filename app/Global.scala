

import play.Application
import play.GlobalSettings
import play.libs.Akka
import scala.concurrent.duration.FiniteDuration
import java.lang.reflect.Method
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.concurrent.TimeUnit;
import play.Logger

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}