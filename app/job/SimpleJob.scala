package job

import akka.actor.Actor
import akka.actor.Cancellable
import play.api.Logger
import javax.inject.Inject
import service.Search

class SimpleJob @Inject() (search: Search) {
   private val logger: Logger = Logger(this.getClass())
  def execute = {
    logger.info("Jobs is executing")
  }
}