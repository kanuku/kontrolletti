package service

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuth
import dao.RepoRepository
import dao.CommitRepository
import javax.inject.Inject
import javax.inject.Singleton
import model.Repository
import model.Commit
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.FutureUtil._
import utility.UrlParser
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import configuration.GeneralConfiguration
import utility.TicketParser

trait ImportRepository {
  def syncApps(): Future[Unit]
}

@Singleton
class ImportRepositoriesImpl @Inject() (oAuthclient: OAuth, kioClient: KioClient,
                                        repoRepo: RepoRepository,
                                        config: GeneralConfiguration) extends ImportRepository with UrlParser {

  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def syncApps(): Future[Unit] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    for {
      accessToken <- logErrorOnFailure(oAuthclient.accessToken())
      kioRepos <- kioClient.repositories(accessToken)
      savedRepos <- repoRepo.all()
      validRepos <- filterValidRepos(kioRepos)
      reposNotInDatabase <- RepositoriesNotInRightHandFilter(validRepos, savedRepos.toList)
      result <- {
        reposNotInDatabase.map { x => (x.url -> x) } match {
          case Nil => Future.successful {}
          case valid =>
            logger.info("from " + kioRepos.size + " Repositories(Kio) only " + valid.size + " are usable and " + savedRepos.size + " are already in database")
            repoRepo.save(valid.toMap.values.toList).map { _ =>
              logger.info("Finished saving apps")
            }
        }
      }
    } yield (savedRepos, result)
  }

  /**
   * Filters repos that have a parsable scm-url. And are therefore valid in Kontrolletti.
   */
  private def filterValidRepos(repositories: List[Repository]): Future[List[Repository]] = Future {
    for {
      repo <- repositories
      if (Option(repo.url) != None && !repo.url.isEmpty())
      (host, project, repository) <- extract(repo.url) match {
        case Right((host, project, repoName)) => Option((host, project, repoName))
        case Left(_)                          => None
      }
    } yield repo.copy(host = host, project = project, repository = repository)
  }

  /**
   * Filters repos from left hand-side that do not exist in the right hand-side. <br>
   * ATTENTION: Valid repos must have been parse successfully!!
   * @param left Valid Repos that need to be filtered.
   * @param right The Valid repos that need to be compared to.
   * @return A Future containing List of Repos from left that do not exist in the right hand-side.
   */
  def RepositoriesNotInRightHandFilter(left: List[Repository], right: List[Repository]): Future[List[Repository]] = Future {
    left.filter {
      n => !right.toList.exists { s => (s.host == n.host && s.project == n.project && s.repository == n.repository) }
    }
  }

}