package job

import akka.actor.Cancellable
import client.oauth.OAuthClient
import javax.inject.Inject
import play.api.Logger
import service.Search
import service.Synchronizer

class SimpleJobDispatcher @Inject() (synchronizer: Synchronizer) {

  private val logger: Logger = Logger(this.getClass())

  def synchronizeApps = {
    synchronizer.syncApps()
  }
}