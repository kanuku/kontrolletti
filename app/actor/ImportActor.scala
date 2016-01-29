package actor

import Job.IMPORT_COMMITS_GITHUB_COM
import Job.IMPORT_COMMITS_GITHUB_ENTERPRISE
import Job.IMPORT_COMMITS_STASH
import Job.IMPORT_REPOSITORIES_KIO
import akka.actor.Actor
import akka.actor.ActorLogging
import configuration.SCMConfiguration
import javax.inject.Inject
import javax.inject.Singleton
import akka.actor.Props
sealed trait Job

object Job {
  case object IMPORT_REPOSITORIES_KIO extends Job
  case object IMPORT_COMMITS_GITHUB_COM extends Job
  case object IMPORT_COMMITS_GITHUB_ENTERPRISE extends Job
  case object IMPORT_COMMITS_STASH extends Job
}

object JobActor{
  def props = Props[JobActor]
  
}

@Singleton
class JobActor  extends Actor with ActorLogging {
  def receive = {
    case IMPORT_REPOSITORIES_KIO =>
      log.info("Importing Repositories from Kio")
      //     repoImporter.syncApps(), 290.seconds)    
      log.info("Finished importing Repositories from Kio")
    case IMPORT_COMMITS_GITHUB_COM =>
      log.info("Importing Commits from Github.com")
//      log.info("############## >>" + config.hosts("github").keys)
      log.info("Finished importing Commits from Github.com")
    //    Await.result(commitImporter.synchCommits(Option("")), 290.seconds)

    case IMPORT_COMMITS_GITHUB_ENTERPRISE =>
      log.info("Importing Commits from Github-Enterprise")
      log.info("Finished importing Commits from Github-Enterprise")
    //    Await.result(commitImporter.synchCommits(Option("")), 290.seconds)
    case IMPORT_COMMITS_STASH =>
      log.info("Importing Commits from stash");
      log.info("Finished importing Commits from stash");
    //    Await.result(commitImporter.synchCommits(Option("")), 290.seconds)
  }

}

 