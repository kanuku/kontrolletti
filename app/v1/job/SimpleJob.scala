package v1.job

import akka.actor.Actor
import akka.actor.Cancellable
import play.api.Logger
object SimpleJob {

  def execute = {
    Logger.info("jobs is executing")
  }
}