package service

import client.oauth.OAuthClient
import javax.inject.Inject
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
/**
 * @author fbenjamin
 */
trait Synchronizer {
  def synchRepositories()
}

class SynchronizerImpl @Inject() (client: OAuthClient) extends Synchronizer {
  val logger: Logger = Logger { this.getClass }
  def synchRepositories() = {
    logger.info("Synchronizing repositories")
    logger.info("Client credentials " + client.clientCredentials())
    logger.info("User credentials " + client.userCredentials())

    client.accessToken().map { x =>
      logger.info("Access tokens" + x)
    }

  }

}