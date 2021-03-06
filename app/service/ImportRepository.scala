package service

import client.kio.KioClient
import client.oauth.OAuth
import configuration.GeneralConfiguration
import dao.{CommitRepository, RepoRepository}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import model.Repository
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import utility.FutureUtil._
import utility.UrlParser

trait ImportRepository {
  def syncApps(): Future[Unit]
}

@Singleton
class ImportRepositoriesImpl @Inject() (oAuthclient: OAuth, kioClient: KioClient,
                                        repoRepo: RepoRepository,
                                        search: Search,
                                        config: GeneralConfiguration) extends ImportRepository with UrlParser {

  val logger: Logger = Logger { this.getClass }

  def syncApps(): Future[Unit] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    val now = System.nanoTime
    for {
      accessToken        <- logErrorOnFailure(oAuthclient.accessToken())
      kioRepos           <- kioClient.repositories(accessToken)
      savedRepos         <- repoRepo.all()
      validRepos         <- keepValidRepos(kioRepos)
      reposNotInDatabase <- notInRightHandFilter(validRepos, savedRepos.toList)
      savedInDatabase    <- Future.traverse(reposNotInDatabase)(saveIfExists).map(_.flatten)
      _                  <- Future.successful {
        logger.info("Result, apps from kio: " + kioRepos.size)
        logger.info("Result, valid apps: " + validRepos.size)
        logger.info("Result, apps already in database:" + savedRepos.size)
        logger.info("Result, new apps saved db:" + savedInDatabase.size)
        val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
        logger.info(s"Result, sync repository job took $elapsed seconds")
      }
    } yield ()
  }

  /**
   * Removes repos that do not have a parsable scm-url or are duplicate.
   */
  private def keepValidRepos(repositories: List[Repository]): Future[List[Repository]] = Future {
    removeDuplicates {
      for {
        repo <- repositories
        if (Option(repo.url) != None && !repo.url.isEmpty())
        (host, project, repoName) <- extract(repo.url) match {
          case Right(result) =>
            logger.info("Parsed " + repo.url + " to " + result)
            Option(result)
          case Left(error) =>
            logger.info("Failed to parse " + repo.url + ": " + error)
            None
        }
      } yield repo.copy(host = host, project = project, repository = repoName)
    }
  }

  /**
   *
   */
  def existsInSCM(repo: Repository): Future[Option[Repository]] = search.isRepo(repo.host, repo.project, repo.repository).map {
      case Right(exists) if exists => Some(repo)
      case _                       => None
  }

  def saveIfExists(repo: Repository): Future[Option[Repository]] = {
    existsInSCM(repo).map {
      case Some(existent) =>
        logger.info("Existing repo will be saved:" + repo.url)
        repoRepo.save(existent)
        Some(existent)
      case None =>
        logger.info("Repo may not exist:" + repo.url)
        None
    }
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
  def notInRightHandFilter(left: List[Repository], right: List[Repository]): Future[List[Repository]] = Future.successful {
    left.filter {
      n => !right.toList.exists { reposAreEqual(_, n) }
    }
  }

  def reposAreEqual(left: Repository, right: Repository) = (left.host == right.host && left.project == right.project && left.repository == right.repository)

}
