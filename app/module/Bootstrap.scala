package module

import service.Import
import javax.inject._
import play.api.Application
import play.api.Logger
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import dao.CommitRepository
import scala.concurrent.Await
import dao.RepoRepository
import dao.AuthorRepository
import model.Author
import model.Author
import model.Link
import akka.actor.ActorSystem
/**
 * @author fbenjamin
 */
trait Bootstrap {
  def setup
}

@Singleton
class BootstrapImpl @Inject() (actorSystem: ActorSystem,
                               importJob: Import,
                               repoRepo: RepoRepository, //
                               authorRepo: AuthorRepository, //
                               commitRepo: CommitRepository) extends Bootstrap {
  val logger: Logger = Logger { this.getClass }

  val star = setup

  def scheduleSyncAppsJob() = actorSystem.scheduler.schedule(12 seconds, 4 minutes) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    Await.result(importJob.syncApps(), 120 seconds)
    logger.info("Finished the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(12 seconds, 1 minutes) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Await.result(importJob.synchCommits(), 20 seconds)
  }
  
  def scheduleDatabaseBootstrap() =
    actorSystem.scheduler.scheduleOnce(7 seconds) {
      logger.info("Started bootstrapping initial database")
      for {
        authorsResult <- authorRepo.initializeDatabase
        repoResult <- repoRepo.initializeDatabase
        commitsResult <- commitRepo.initializeDatabase
      } yield (authorsResult)
    }

 

  def setup() = {
//    scheduleSyncAppsJob
    scheduleSynchCommitsJobs
    
  }

}