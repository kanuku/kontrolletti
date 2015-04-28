package v1.job

import akka.actor.Actor
import akka.actor.Cancellable
import play.api.Logger
import javax.inject.Inject
import v1.service.Search


class SimpleJob @Inject() (search:Search){

  def execute = {
    Logger.info("Jobs is executing")
  }
}