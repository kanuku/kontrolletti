package module

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import dao.{ CommitRepository, RepoRepository }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import service.{ ImportCommit, ImportRepository }
/**
 * @author fbenjamin
 */
trait Bootstrap {
  def setup()
}

@Singleton
class BootstrapImpl @Inject() (actorSystem: ActorSystem,
                               repoImporter: ImportRepository,
                               commitImporter: ImportCommit,
                               repoRepo: RepoRepository, //
                               commitRepo: CommitRepository) extends Bootstrap {
  val logger: Logger = Logger { this.getClass }

  val star = setup()

  def scheduleSyncAppsJob() = actorSystem.scheduler.schedule(12.seconds, 120.seconds) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    Await.result(repoImporter.syncApps(), 180.seconds)
    logger.info("Finished the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(500.seconds, 20.seconds) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Await.result(commitImporter.synchCommits(), 300.seconds)
  }

  def scheduleDatabaseBootstrap() = actorSystem.scheduler.scheduleOnce(7.seconds) {
    logger.info("Started bootstrapping initial database")
    for {
      repoResult <- repoRepo.initializeDatabase
      commitsResult <- commitRepo.initializeDatabase
    } yield (commitsResult)
  }

  def setup() = {
    scheduleSyncAppsJob
    scheduleSynchCommitsJobs
  }

}