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
    
    oAuthclient.accessToken().onSuccess {
      case accessToken =>
        logger.info("Received an accessToken")
        kioClient.apps(accessToken).onSuccess {
          case apps =>
            logger.info("Received apps from kio")
            store.saveAppInfo(apps)
        }

    }
  }
}