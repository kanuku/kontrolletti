package module

import akka.actor.ActorSystem
import dao.{CommitRepository, RepoRepository}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}
import service.{ImportCommit, ImportRepository}

/**
 * @author fbenjamin
 */
trait Bootstrap {
  def setup(): Unit
}

@Singleton
class BootstrapImpl @Inject() (actorSystem: ActorSystem,
                               repoImporter: ImportRepository,
                               commitImporter: ImportCommit,
                               repoRepo: RepoRepository, //
                               commitRepo: CommitRepository) extends Bootstrap {
  val logger: Logger = Logger { this.getClass }

  def syncRepoJob = for {
    _ <- Future.successful(logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO"))
    _ <- repoImporter.syncApps
    _ <- Future.successful(logger.info("Finished the synch job for synchronizing AppInfos(SCM-URL's) from KIO"))
  } yield ()

  def syncCommitsJob = for {
    _ <- Future.successful(logger.info("Started the job for synchronizing Commits from the SCM's"))
    _ <- commitImporter.synchCommits
    _ <- Future.successful(logger.info("Finished the job for synchronizing Commits from the SCM's"))
  } yield ()

  def runJobs(): Unit = {
    val j = for {
      _ <- syncRepoJob recover { case e: Throwable => logger.error("Sync repository job failed.", e) }
      _ <- syncCommitsJob recover { case e: Throwable => logger.error("Sync commit job failed.", e) }
    } yield ()

    j onComplete { _ =>
      actorSystem.scheduler.scheduleOnce(288.seconds)(setup)
    }
  }

  def setup(): Unit = {
    actorSystem.scheduler.scheduleOnce(12.seconds)(runJobs)
    ()
  }

  setup
}
