package service

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuth
import javax.inject.Inject
import javax.inject.Singleton
import model.AppInfo
import model.Commit
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.UrlParser
import dao.AppInfoRepository
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import utility.FutureUtil._
/**
 * @author fbenjamin
 */

trait Import {

  def syncApps(): Future[Unit]
  def synchCommits(): Future[Unit]
}

@Singleton
class ImportImpl @Inject() (oAuthclient: OAuth, store: DataStore, //
                            kioClient: KioClient, search: Search, //
                            appRepo: AppInfoRepository) extends Import with UrlParser {
  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def syncApps(): Future[Unit] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    oAuthclient.accessToken().flatMap { accessToken =>
      logger.info("Received an accessToken")
      logErrorOnFailure(kioClient.apps(accessToken)).flatMap { apps =>
        logger.info("Number of received apps from kio:" + apps.size)
        appRepo.save(filterValidApps(apps))
      }
    }
  }

  /**
   * Filter apps that have a parsable scm-url. And are not already in the datastore.
   */
  private def filterValidApps(apps: List[AppInfo]): List[AppInfo] = apps.filter { x =>
    extract(x.scmUrl) match {
      case Right(_) => true
      case _        => false
    }
  }

  def synchCommits(): Future[Unit] = store.scmUrls().flatMap { x =>
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Future {
      x.map { input =>
        logger.info(s"Synchronizing $input")
        search.parse(input) match {
          case Right((host, project, repository)) =>
            logger.info(s"Synchronized $input successfully!")
            synchCommit(host, project, repository)
          case Left(_) =>
            logger.warn(s"Failed to synchronize $input!")
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