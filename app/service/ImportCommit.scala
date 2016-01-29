package service

import scala.concurrent.Future
import client.kio.KioClient
import client.oauth.OAuth
import dao.RepoRepository
import dao.CommitRepository
import javax.inject.{ Inject, Singleton }
import model.Repository
import model.Commit
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.FutureUtil._
import utility.UrlParser
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import configuration.GeneralConfiguration
import utility.GeneralHelper
import utility.TicketParser

trait ImportCommit {
  def synchCommits(): Future[Unit]
}

@Singleton
class ImportCommitImpl @Inject() (oAuthclient: OAuth, commitRepo: CommitRepository, //
                                  search: Search, //
                                  repoRepo: RepoRepository,
                                  config: GeneralConfiguration) extends ImportCommit with TicketParser with GeneralHelper {

  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def synchCommits(): Future[Unit] = Future {
    repoRepo.enabled().map { repos =>
      logger.info("Started the job for synchronizing Commits from " + repos.size + " Repositories")
      repos.map { repo =>
        commitRepo.youngest(repo.url).map { lastCommit =>
          logger.info("Last commit:" + lastCommit)
          synchCommit(repo, lastCommit)
        }
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
          val commitsWithoutDuplicates = removeIfExists(result, since)
          logger.info("From the " + result.size + " commits, only " + commitsWithoutDuplicates.size + " are not already saved in the database.")
          val updatedCommits = updateCommits(repo, commitsWithoutDuplicates)
          commitRepo.save(updatedCommits).flatMap { _ =>
            logger.info(s"Saved " + result.size + "commits from " + repo.url)
            synchCommit(repo, since, pageNumber + 1)
          }
      }
    }
  }

  def removeIfExists(commits: List[Commit], commit: Option[Commit]) = commit match {
    case Some(commit2Remove) =>
      for (commit2Check <- commits if (commit2Remove.id != commit2Check.id) && commit2Check.date.isAfter(commit2Remove.date))
        yield commit2Check
    case None => commits

  }

  def updateCommits(repo: Repository, commits: List[Commit]): List[Commit] = for {
    commit <- enrichWithTickets(repo.host, repo.project, repo.repository, commits)
  } yield commit.copy(repoUrl = repo.url)

  private def commits(repository: Repository, since: Option[Commit], pageNumber: Int): Future[Option[List[Commit]]] =
    search.commits(repository.host, repository.project, repository.repository, since, None, pageNumber).map {
      _ match {
        case Right(Some(Nil)) =>
          logger.info(s"Received empty result from $repository")
          None
        case Right(Some(result)) =>
          logger.info("About to import " + result.size + " commits from " + repository.url)
          Some(result)
        case Right(None) =>
          logger.info(s"Received no result from $repository")
          None
        case Left(msg) =>
          logger.warn(s"Received msg: $msg")
          None
      }
    }

  /**
   * Will parse the commit-message in each commit and add a ticket to the commit if the parser returns a ticket.
   * @param host Hostname where the commits are originated from.
   * @param project Project to where the commits belong to.
   * @param repository Repository where the commits come from.
   * @param commits Commits whom's commit-messages should be parsed
   * @return The same commit
   */

  def enrichWithTickets(host: String, project: String, repository: String, commits: List[Commit]): List[Commit] = {
    for {
      commit <- commits
      result = parse(host, project, repository, commit.message) match {
        case None         => commit.copy(valid = Option(numberOfTickets(commit.tickets) > 0))
        case Some(ticket) => commit.copy(tickets = Option(List(ticket)), valid = Option(numberOfTickets(Option(List(ticket))) > 0))
      }
    } yield result
  }

  def jiraTicketUrl: String = config.ticketReferenceJiraBrowseUrl
  def githubHost: String = config.ticketReferenceGithubHost
  def githubEnterpriseHost: String = config.ticketReferenceGithubEnterpriseHost

}