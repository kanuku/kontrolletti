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
  }

  def scheduleSynchCommitsJobs() = actorSystem.scheduler.schedule(12 seconds, 1 minutes) {
    logger.info("Started the job for synchronizing Commits from the SCM's")
    Await.result(importJob.synchCommits(), 8 minutes)
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

  def saveAuthor() = actorSystem.scheduler.schedule(10 seconds, 5 minutes ) {
    //    val link1 = new Link("href1", "method", "rel", "relType")
    //    val link2 = new Link("href2", "method", "rel", "relType")
    //    val link3 = new Link("href3", "method", "rel", "relType")
    //    val author = new Author("I. Should", "IShouldNot@name.it", Some(List(link1, link2, link3)))
    //    authorRepo.save(List(author))
    authorRepo.list().map { x =>
      println(x)
    }
  }

  def setup() = {
//    scheduleDatabaseBootstrap
//            saveAuthor 
    scheduleSyncAppsJob
    scheduleSynchCommitsJobs
  }

}