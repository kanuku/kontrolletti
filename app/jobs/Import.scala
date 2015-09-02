package jobs

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuth
import javax.inject.Inject
import javax.inject.Singleton
import model.AppInfo
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration.DurationInt
import service.DataStore
import service.Search
import utility.UrlParser
import model.Commit
import scala.util.Failure
import scala.util.Success
import play.api.Application
import com.google.inject.ImplementedBy
import akka.actor.ActorSystem


/**
 * @author fbenjamin
 */

@ImplementedBy(classOf[ImportImpl])
trait Import {

  def syncApps(): Future[Boolean]

  def synchCommits(): Future[List[Future[Boolean]]]
}

@Singleton
class ImportImpl @Inject() (oAuthclient: OAuth, //
                            store: DataStore, //
                            kioClient: KioClient, //
                            search: Search,
                            actorSystem: ActorSystem) extends Import with UrlParser {
  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)
  
  
  val syncAppsJob=scheduleSyncAppsJob
  val synchCommitsJobs=scheduleSynchCommitsJobs
   

  def syncApps(): Future[Boolean] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    oAuthclient.accessToken().flatMap { accessToken =>
      logger.info("Received an accessToken")
      kioClient.apps(accessToken).flatMap { apps =>
        logger.info("Received apps from kio")
        store.saveAppInfo(filterValidApps(apps))
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

  def synchCommits(): Future[List[Future[Boolean]]] = store.scmUrls().flatMap { x =>
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Future {
      x.map { input=>
        logger.info(s"Synchronizing $input")
        search.parse(input) match {
          case Right((host, project, repository)) =>
            logger.info(s"Synchronized $input successfully!")
            synchCommit(host, project, repository)
          case Left(_) => 
            logger.info(s"Failed to synchronize $input!")
            Future.successful(false)
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

  
  def scheduleSyncAppsJob() = {
    actorSystem.scheduler.schedule(0 minutes, 60 minutes) {
      logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
      syncApps()
    }
  }
  def scheduleSynchCommitsJobs() = {
	  actorSystem.scheduler.schedule(0 minutes, 40 minutes) {
		  logger.info("Started the job for synchronizing Commits from the SCM's")
		  synchCommits()
	  }
  }
  def test() = {
	  actorSystem.scheduler.schedule(0 minutes, 15 seconds) {
		  logger.info("Started the job for synchronizing Commits from the SCM's")
		  
	  }
  }

}