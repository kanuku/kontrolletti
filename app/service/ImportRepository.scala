package service

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import client.kio.KioClient
import client.oauth.OAuth
import configuration.GeneralConfiguration
import dao.{ CommitRepository, RepoRepository }
import javax.inject.{ Inject, Singleton }
import model.Repository
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.FutureUtil._
import utility.UrlParser
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.util.Failure
import scala.util.Success

trait ImportRepository {
  def syncApps(): Future[Unit]
}

@Singleton
class ImportRepositoriesImpl @Inject() (oAuthclient: OAuth, kioClient: KioClient,
                                        repoRepo: RepoRepository,
                                        search: Search,
                                        config: GeneralConfiguration) extends ImportRepository with UrlParser {

  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def syncApps(): Future[Unit] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    for {
      accessToken <- logErrorOnFailure(oAuthclient.accessToken())
      kioRepos <- kioClient.repositories(accessToken)
      savedRepos <- repoRepo.all()
      validRepos <- keepValidRepos(kioRepos)
      reposNotInDatabase <- notInRightHandFilter(validRepos, savedRepos.toList)
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
   * Removes repos that do not have a parsable scm-url or are duplicate, or that
   */
  private def keepValidRepos(repositories: List[Repository]): Future[List[Repository]] = Future {
    removeDuplicates {
      for {
        repo <- repositories
        if (Option(repo.url) != None && !repo.url.isEmpty())
        (host, project, repoName) <- extract(repo.url) match {
          case Right(result) => Option(result)
          case Left(error)   => None
        }
        if exists(host, project, repoName)

      } yield repo.copy(host = host, project = project, repository = repoName)
    }
  }

  /**
   *
   */
  def exists(host: String, project: String, repository: String): Boolean = {
    Await.result(tryFuture(search.isRepo(host, project, repository).map { result =>
      result match {
        case Left(error) =>
          logger.warn(s"Project might not exist $host - $project - $repository")
          false
        case Right(value) => value
      }
    }, Future.successful(false)), Duration("30 seconds"))
  }

  def removeDuplicates(repos: List[Repository]): List[Repository] = repos match {
    case head :: tail => if (tail.exists { reposAreEqual(_, head) }) removeDuplicates(tail) else head :: removeDuplicates(tail)
    case Nil          => Nil
  }

  /**
   * Filters repos from left hand-side that do not exist in the right hand-side. <br>
   * ATTENTION: Valid repos must have been parse successfully!!
   * @param left Valid Repos that need to be filtered.
   * @param right The Valid repos that need to be compared to.
   * @return A Future containing List of Repos from left that do not exist in the right hand-side.
   */
  def notInRightHandFilter(left: List[Repository], right: List[Repository]): Future[List[Repository]] = Future {
    left.filter {
      n => !right.toList.exists { reposAreEqual(_, n) }
    }
  }

  def reposAreEqual(left: Repository, right: Repository) = (left.host == right.host && left.project == right.project && left.repository == right.repository)

}