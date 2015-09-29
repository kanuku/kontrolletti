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

/**
 * @author fbenjamin
 */

trait Import {

  def syncApps(): Future[Unit]
  def synchCommits(): Future[Unit]
}

@Singleton
class ImportImpl @Inject() (oAuthclient: OAuth, commitRepo: CommitRepository, //
                            kioClient: KioClient, search: Search, //
                            repoRepo: RepoRepository) extends Import with UrlParser {

  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def syncApps(): Future[Unit] = {
    logger.info("Started the synch job for synchronizing AppInfos(SCM-URL's) from KIO")
    for {
      accessToken <- logErrorOnFailure(oAuthclient.accessToken())
      kioRepos <- kioClient.apps(accessToken)
      savedRepos <- repoRepo.all()
      result <- {
        val notInDb = kioRepos.filter { n => !savedRepos.toList.exists { s => (s.url == n.url) } }
        val filtered = filterValidRepos(notInDb)
        val valid = filtered.map { x => (x.url -> x) }.toMap
        logger.info("from " + kioRepos.size + " Repositories(Kio) only " + valid.size + " are usable and " + savedRepos.size + " are already in database")
        repoRepo.save(valid.values.toList).map { x =>
          logger.info("Finished saving apps")
        }
      }
    } yield (savedRepos, result)
  }

  /**
   * Filter apps that have a parsable scm-url. And are not already in the datastore.
   */
  private def filterValidRepos(repositories: List[Repository]): List[Repository] = for {
    repo <- repositories
    if (Option(repo.url) != None && !repo.url.isEmpty())
    (host, project, repository) <- extract(repo.url) match {
      case Right((host, project, repoName)) => Option((host, project, repoName))
      case Left(_)                          => None
    }
  } yield repo.copy(host = host, project = project, repository = repository)

  def synchCommits(): Future[Unit] = repoRepo.enabled().map { repos =>
    logger.info("Started the job for synchronizing Commits from " + repos.size + " Repositories")
    repos.map { repo =>
      commitRepo.youngest(repo.url).flatMap { lastCommit =>
        logger.info("Last commit:" + lastCommit)
        synchCommit(repo, lastCommit)
      }
    }
  }
  private def synchCommit(repo: Repository, since: Option[Commit], pageNumber: Int = 1): Future[Boolean] = {
    logger.info(s"Getting Commit's page nr:$pageNumber from:" + repo.url)
    commits(repo, since, pageNumber).flatMap {
      _ match {
        case None =>
          logger.info("No result")
          falseFuture
        case Some(result) =>
          val updateCommits = removeIfExists(updateChildIds(repo, result), since)
          logger.info("result: " + result.size + " - updated: " + updateCommits.size)
          commitRepo.save(updateCommits).flatMap { _ =>
            logger.info(s"Saved " + result.size + "commits from " + repo.url)
            synchCommit(repo, since, pageNumber + 1)
          }
      }
    }
  }

  def removeIfExists(commits: List[Commit], commit: Option[Commit]) = commit match {
    case Some(commit2Remove) =>
      for (commit2Check <- commits if (commit2Remove.id != commit2Check.id))
        yield commit2Check
    case None => commits

  }

  
  def updateChildIds(repo: Repository, commits: List[Commit]): List[Commit] = for {
    parent <- commits
    result <- commits.find { _.parentIds.contains(parent.id) } match {
      case None        => List(parent.copy(repoUrl = repo.url))
      case Some(child) => List(parent.copy(repoUrl = repo.url, childId = Option(child.id)))
    }
  } yield result

  private def commits(repository: Repository, since: Option[Commit], pageNumber: Int): Future[Option[List[Commit]]] =
    search.commits(repository.host, repository.project, repository.repository, since, None, pageNumber).map {
      _ match {
        case Right(Some(Nil)) =>
          logger.info("Received empty result from $repository")
          None
        case Right(Some(result)) =>
          logger.info("About to import " + result.size + " commits from " + repository.url)
          Some(result)
        case Right(None) =>
          logger.info("Received no result from $repository")
          None
        case Left(msg) =>
          logger.warn(s"Received msg: $msg")
          None
      }
    }

}