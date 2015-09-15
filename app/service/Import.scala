package service

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuth
import dao.AppInfoRepository
import javax.inject.Inject
import javax.inject.Singleton
import model.AppInfo
import model.Commit
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.FutureUtil._
import utility.UrlParser
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
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
    for {
      accessToken <- logErrorOnFailure(oAuthclient.accessToken())
      kioApps <- kioClient.apps(accessToken)
      savedApps <- appRepo.list()
      result <- {
        val notInDb = kioApps.filter { n => !savedApps.toList.exists { s => (s.scmUrl == n.scmUrl) } }
        val filtered = filterValidApps(notInDb)
        val valid = filtered.map { x => (x.scmUrl -> x) }.toMap
        logger.info("from " + kioApps.size + " Apps(Kio) only " + valid.size + " are usable and " + savedApps.size + " are already in database")
        appRepo.saveApps(valid.values.toList).map { x =>
          logger.info("Finished saving apps")
        }
      }
    } yield (savedApps, result)
  }

  /**
   * Filter apps that have a parsable scm-url. And are not already in the datastore.
   */
  private def filterValidApps(apps: List[AppInfo]): List[AppInfo] = apps.filter {
    _.scmUrl match {
      case url if (Option(url) != None && !url.isEmpty()) =>
        extract(url) match {
          case Right(_) => true
          case _        => false
        }
      case _ => false
    }
  }

  def synchCommits(): Future[Unit] = {
    appRepo.scmUrls.map { urls =>
      logger.info("Started the job for synchronizing Commits from " + urls.size + " Repositories")

      urls.map { input =>
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

  private def synchCommit(host: String, project: String, repository: String): Future[Boolean] = {

    commits(host, project, repository).flatMap {
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