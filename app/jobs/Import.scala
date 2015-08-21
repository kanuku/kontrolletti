package jobs

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuthClient
import javax.inject.Inject
import javax.inject.Singleton
import model.AppInfo
import model.AppInfo
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import service.DataStore
import service.Search
import utility.UrlParser
import model.Commit
import scala.util.Failure
import scala.util.Success

/**
 * @author fbenjamin
 */
trait Import {

  def syncApps(): Future[Boolean]

  def synchCommits(): Future[List[Future[Boolean]]]
}

@Singleton
class ImportImpl @Inject() (oAuthclient: OAuthClient, //
                            store: DataStore, //
                            kioClient: KioClient, //
                            search: Search) extends Import with UrlParser {
  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

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

  def synchCommits(): Future[List[Future[Boolean]]] = store.scmUrls().flatMap { x =>
    Future {
      x.map {
        search.parse(_) match {
          case Right((host, project, repository)) =>
            synchCommit(host, project, repository)
          case Left(_) => Future.successful(false)
        }
      }
    }
  }

  private def synchCommit(host: String, project: String, repository: String): Future[Boolean] = commits(host, project, repository).flatMap {
    _ match {
      case Some(result) => store.saveCommits(result).map { saved =>
        logger.info(s"saved=($saved) for $result.size commits from $host/$project$host")
        saved
      }
      case None =>
        logger.info("No result")
        falseFuture
    }
  }

  private def commits(host: String, project: String, repository: String): Future[Option[List[Commit]]] = {
    search.commits(host, project, repository, None, None).map { x =>
      x match {
        case Right(Some(result)) =>
          logger.info("About to import " + result.size + s" commits from $host/$project$host")
          Some(result)
        case _ =>
          logger.info("Received no usefull result from $host/$project$host")
          None
      }
    }
  }

}