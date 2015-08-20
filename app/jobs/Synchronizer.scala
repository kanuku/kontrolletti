package jobs

import scala.concurrent.Future
import client.oauth.OAuthClient
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import client.kio.KioClient
import model.AppInfo
import utility.UrlParser
import service.DataStore
import service.Search
import model.AppInfo

/**
 * @author fbenjamin
 */
trait Synchronizer {

  /**
   * Synchroni
   */
  def syncApps(): Future[Boolean]

  def synchCommits(): Future[Boolean]
}

@Singleton
class SynchronizerImpl @Inject() (oAuthclient: OAuthClient, //
                                  store: DataStore, //
                                  kioClient: KioClient, //
                                  search:Search
                                  ) extends Synchronizer with UrlParser {
  val logger: Logger = Logger { this.getClass }

  def syncApps(): Future[Boolean] = {
    oAuthclient.accessToken().flatMap { accessToken =>
      logger.info("Received an accessToken")
      kioClient.apps(accessToken).flatMap { apps =>
        logger.info("Received apps from kio")
        store.saveAppInfo(filterValidApps(apps))
      }
    }
  }

  /**
   * Only apps that have a parsable scm-url should be accepted into the store.
   */
  private def filterValidApps(apps: List[AppInfo]): List[AppInfo] = apps.filter { x =>
    extract(x.scmUrl) match {
      case Right(_) => true
      case _        => false
    }
  }

  def synchCommits(): Future[Boolean] = {

    Future.successful(false)
  }
}