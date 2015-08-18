package job

import akka.actor.Actor
import akka.actor.Cancellable
import play.api.Logger
import javax.inject.Inject
import service.Search
import client.oauth.OAuthClient
import service.Synchronizer

class SimpleJob @Inject() (synchronizer: Synchronizer) {
  private val logger: Logger = Logger(this.getClass())
  def execute = {
    synchronizer.synchRepositories()
  }
}