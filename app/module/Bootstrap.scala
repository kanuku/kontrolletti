package module

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import java.util.concurrent.TimeUnit
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

  def scheduleSyncAppsJob() = actorSystem.scheduler.schedule(12.seconds, 600.seconds) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    val now = System.nanoTime
    Await.result(repoImporter.syncApps(), 590.seconds)
    val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
    logger.info(s"Result, job took $elapsed seconds")
    logger.info("Finished the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(612.seconds, 600.seconds) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    val now = System.nanoTime
    Await.result(commitImporter.synchCommits(), 590.seconds)
    val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
    logger.info(s"Result, job took $elapsed seconds")
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