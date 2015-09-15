package module

import dao.AppInfoRepository
import service.Import
import javax.inject._
import play.api.Application
import play.api.Logger
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import dao.CommitRepository
import scala.concurrent.Await
/**
 * @author fbenjamin
 */
trait Bootstrap {
  def setup
}

@Singleton
class BootstrapImpl @Inject() (importJob: Import,
                               appsRepo: AppInfoRepository, commitRepo: CommitRepository, app: Application) extends Bootstrap {
  val logger: Logger = Logger { this.getClass }

  val star = setup

  def scheduleSyncAppsJob() = app.actorSystem.scheduler.schedule(10 seconds, 10 minutes) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    Await.result(importJob.syncApps(), 20 seconds)
  }

  def scheduleSynchCommitsJobs() = app.actorSystem.scheduler.schedule(10 seconds, 20 seconds) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Await.result(importJob.synchCommits(), 20 seconds)
  }

  
  def scheduleDatabaseBootstrap() =
    app.actorSystem.scheduler.scheduleOnce(10 seconds) {
      logger.info("Started bootstrapping initial database")
      for {
        appsResult <- appsRepo.initializeDatabase
        commitsResult <- commitRepo.initializeDatabase
      } yield (appsResult, commitsResult)
    }
 
  
  def setup() = {
//    scheduleDatabaseBootstrap
//    scheduleSyncAppsJob
    scheduleSynchCommitsJobs
  }

}