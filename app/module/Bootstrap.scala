package module

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem
import dao.{ CommitRepository, RepoRepository }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import service.{ ImportCommit, ImportRepository }
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorRef
import akka.actor.Props
import akka.contrib.throttle.TimerBasedThrottler
import akka.contrib.throttle.Throttler._
import akka.contrib.throttle._
import javax.inject.Named
import scala.concurrent.ExecutionContext
import actor.Job._
trait Bootstrap {
  def setup()
}

@Singleton
class BootstrapImpl @Inject() (actorSystem: ActorSystem,
                               repoImporter: ImportRepository,
                               commitImporter: ImportCommit,
                               repoRepo: RepoRepository, //
                               commitRepo: CommitRepository, //
                               @Named("job-actor") configuredActor: ActorRef //
                               )(implicit ec: ExecutionContext) extends Bootstrap {
  val logger: Logger = Logger { this.getClass }

  val star = setup()

  val throttler = actorSystem.actorOf(Props(new TimerBasedThrottler(3 msgsPer (1.second))).withDispatcher("job-akka"))
   throttler.tell(new Throttler.SetTarget(configuredActor), null);
  def setup() = {
    for (a <- 1 to 3) {
      actorSystem.scheduler.schedule(10.seconds, 3.seconds) {
        throttler.tell(IMPORT_REPOSITORIES_KIO, configuredActor)
      }
    }
    //    scheduleSyncAppsJob
    //    scheduleSynchCommitsJobs
    
    

    //    for (a <- 1 to 400) {
    //      actorSystem.scheduler.schedule(10.seconds, 10.seconds) {
    //        
    //        
    //      }
    //    }
    //    actorSystem.scheduler.schedule(21.seconds, 10.seconds) {
    //      val msg = "job2"
    //      start(msg, 10.seconds)
    //      finish(msg)
    //    }
    //    actorSystem.scheduler.schedule(42.seconds, 10.seconds) {
    //      val msg = "job3"
    //      start(msg, 10.seconds)
    //      finish(msg)
    //    }
    //    actorSystem.scheduler.schedule(63.seconds, 10.seconds) {
    //      val msg = "job4"
    //      start(msg, 10.seconds)
    //      finish(msg)
    //    }
  }

  def start(msg: String, wait: FiniteDuration) {
    val f = Future {
      Thread.sleep(wait.toSeconds)
      logger.info(s"Waiting $msg")
    }
    Await.result(f, wait)
    finish(msg)
  }

  def finish(msg: String) {
    logger.info(s"Finished $msg")
  }
  def scheduleSyncAppsJob() = actorSystem.scheduler.schedule(12.seconds, 300.seconds) {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    //    Await.result(repoImporter.syncApps(), 290.seconds)
    logger.info("Finished the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(312.seconds, 300.seconds) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    //    Await.result(commitImporter.synchCommits(), 290.seconds)
  }

  def scheduleDatabaseBootstrap() = actorSystem.scheduler.scheduleOnce(7.seconds) {
    logger.info("Started bootstrapping initial database")
    for {
      repoResult <- repoRepo.initializeDatabase
      commitsResult <- commitRepo.initializeDatabase
    } yield (commitsResult)
  }

}