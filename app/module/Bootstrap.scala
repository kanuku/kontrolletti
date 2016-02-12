package module

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem
import dao.{ CommitRepository, RepoRepository }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import service.{ ImportCommit, ImportRepository }
import java.util.concurrent.TimeUnit
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

  def scheduleSyncAppsJob() = actorSystem.scheduler.schedule(12.seconds, 300.seconds) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    val now = System.nanoTime
    Await.result(repoImporter.syncApps(), 290.seconds)
    val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
    logger.info(s"Repos-job took $elapsed seconds")
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(312.seconds, 300.seconds) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    val now = System.nanoTime
    Await.result(commitImporter.synchCommits(), 290.seconds)
    val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
    logger.info(s"Commits-job took $elapsed seconds")
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
    //    scheduleSynchCommitsJobs
  }

}