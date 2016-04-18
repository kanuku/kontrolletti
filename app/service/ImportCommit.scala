package service

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import client.oauth.OAuth
import configuration.GeneralConfiguration
import dao.{CommitRepository, RepoRepository}
import model.{Commit, Repository}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utility.FutureUtil._
import utility.{GeneralHelper, TicketParser}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.control.NonFatal

trait ImportCommit {
  def synchCommits(): Future[Unit]
  def enrichWithTickets(host: String, project: String, repository: String, commits: List[Commit]): List[Commit]
}

@Singleton
class ImportCommitImpl @Inject() (actorSystem: ActorSystem,
                                  oAuthclient: OAuth, commitRepo: CommitRepository, //
                                  search: Search, //
                                  repoRepo: RepoRepository,
                                  config: GeneralConfiguration) extends ImportCommit with TicketParser with GeneralHelper {

  val logger: Logger = Logger { this.getClass }

  val falseFuture = Future.successful(false)

  def synchCommits(): Future[Unit] = {

    val now = System.nanoTime

    for {
      repos <- repoRepo.enabled()
      _     <- Future.successful(logger.info("Started the job for synchronizing Commits from " + repos.size + " Repositories"))
      _     <- doSyncCommit(repos)
      _     <- Future.successful {
        val elapsed = TimeUnit.SECONDS.convert((System.nanoTime - now), TimeUnit.NANOSECONDS)
        logger.info(s"Result, sync commits job took $elapsed seconds")
      }
    } yield ()
  }

  private def synchCommit(repo: Repository, since: Option[Commit], pageNumber: Int = 1): Future[Boolean] = {
    logger.info(s"Getting Commit's page/position :$pageNumber from:" + repo.url)
    def runSyncCommit = commits(repo, since, pageNumber).flatMap {
      case None =>
        logger.info("No result")
        falseFuture
      case Some(result) =>
        val commitsWithoutDuplicates = removeIfExists(result, since)
        logger.info("From the " + result.size + " commits, only " + commitsWithoutDuplicates.size + " are not already saved in the database.")
        val updatedCommits = updateCommits(repo, commitsWithoutDuplicates)
        commitRepo.save(updatedCommits).flatMap { _ =>
          logger.info(s"Saved " + result.size + "commits from " + repo.url)
          search.nextPosition(repo.host, pageNumber, result.size) match {
            case Right(pos) =>
              synchCommit(repo, since, pos)
            case Left(msg)  =>
              logger.error(s"Sync commit for repo [$repo] finished with error: " + msg)
              Future.successful(false)
          }
        }
    }

    for {
      _ <- timeoutFuture(actorSystem, 1.seconds) // interval between getting two pages of commit
      b <- runSyncCommit recover {
        case NonFatal(e) =>
          logger.error(s"Failed to import commits for repo: $repo - since: $since - page: $pageNumber", e)
          false
      }
    } yield b
  }

  private def doSyncCommit(repos: Seq[Repository]): Future[Unit] = {
    // run future one after another
    repos.foldLeft(Future.successful(())) { case (acc, repo) =>
      for {
        _ <- acc
        lastCommit <- commitRepo.youngest(repo.url)
        _ <- timeoutFuture(actorSystem, 1.seconds) // interval between import commits for two repo
        _ <- Future.successful(logger.info("Last commit: " + lastCommit))
        _ <- synchCommit(repo, lastCommit)
      } yield ()
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
        case Some(ticket) => commit.copy(tickets = Option(List(ticket)), valid = Option(numberOfTickets(Option(List(ticket))) > 0 || commit.parentIds.toList.nonEmpty))
      }
    } yield result
  }

  def jiraTicketUrl: String = config.ticketReferenceJiraBrowseUrl
  def githubHost: String = config.ticketReferenceGithubHost
  def githubEnterpriseHost: String = config.ticketReferenceGithubEnterpriseHost

}
