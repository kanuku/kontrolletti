package service

import dao.{ CommitRepository, RepoRepository }
import javax.inject.{Inject, Singleton}
import model.Repository
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.{ ExecutionContext, Future }

trait UpdateCommit {
  /**
    * This method will re-check existing commits and apply validation
    * It's very dangerous because it could fill up all memory.
    * TODO: replace with stream based solution.
    */
  def updateCommitsTicket(): Future[Unit]
}

@Singleton
class UpdateCommitImpl @Inject() (
  commitRepo: CommitRepository,
  repoRepo: RepoRepository,
  importCommit: ImportCommit
) extends UpdateCommit {

  def updateCommitsTicket() = for {
    repos <- repoRepo.enabled
    _ <- updateTicket(repos)
  } yield ()


  private def updateTicket(repos: Seq[Repository]): Future[Unit] =
    repos.foldLeft(Future.successful(())) { case (acc, repo) =>
      for {
        _ <- acc
        _ <- updateTicketInRepo(repo)
      } yield ()
    }

  private def updateTicketInRepo(repo: Repository): Future[Unit] =
    commitRepo.byRepoUrl(repo.url)
      .flatMap(cs => {
        val enriched = importCommit.enrichWithTickets(repo.host, repo.project,
          repo.repository, cs.toList)
        commitRepo.save(enriched)
      }
    )
}
