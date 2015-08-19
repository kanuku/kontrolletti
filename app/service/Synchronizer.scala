package service

import scala.concurrent.Future
import client.oauth.OAuthClient
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import client.kio.KioClient

/**
 * @author fbenjamin
 */
trait Synchronizer {
  def syncApps()
}

@Singleton
class SynchronizerImpl @Inject() (oAuthclient: OAuthClient, store: DataStore, kioClient: KioClient) extends Synchronizer {
  val logger: Logger = Logger { this.getClass }
  def syncApps() = {
    oAuthclient.accessToken().map { x =>
      logger.info("TOKEN " + x)
    }
  }

}