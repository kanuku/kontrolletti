package dao

import scala.concurrent.{ ExecutionContext, Future }
import org.joda.time.DateTime
import dao.KontrollettiPostgresDriver.api._
import javax.inject.{ Inject, Singleton }
import model.Commit
import model.Repository
import model.Ticket
import play.api.Logger
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider }
import utility.FutureUtil
import model.Repository
import dao.Tables.CommitTable
import dao.Tables.RepositoryTable

/**
 * @author fbenjamin
 */

trait CommitRepository {

  def initializeDatabase: Future[Unit]
  def all(): Future[Seq[Commit]]
  def save(commits: List[Commit]): Future[Unit]
  def byId(info: RepoParameters, id: String): Future[Option[Commit]]
  def byRepoUrl(repoUrl: String): Future[Seq[Commit]]
  def get(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Commit]]
  def youngest(repoUrl: String): Future[Option[Commit]]
  def oldest(repoUrl: String): Future[Option[Commit]]
  def tickets(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Ticket]]

}

@Singleton
class CommitRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommitRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  private val logger: Logger = Logger(this.getClass())
  private val commits = Tables.commits
  private val repos = Tables.repositories
  private val defaultPageNumber = 1
  private val defaultMaxPerPage = 500
  private val defaultPerPage = 50

  def initializeDatabase = {
    logger.info("Started initializing the Commit table!")
    db.run { commits.schema.create }.map { x =>
      logger.info("Fisnihed initializing the Commit table!")
    }
  }

  def all(): Future[Seq[Commit]] = handleError(db.run(commits.result))

  def save(input: List[Commit]): Future[Unit] = {
    logger.info("saving " + input.size + " commits")
    val action = DBIO.sequence(for {
      i <- input
    } yield commits.insertOrUpdate(i)).transactionally
    handleError(db.run(action).map(_ => ()))
  }

  def byId(info: RepoParameters, id: String): Future[Option[Commit]] = db.run {
    getByRepositoryQuery(info, None).filter(_.id === id).result.headOption
  }

  def byRepoUrl(repoUrl: String): Future[Seq[Commit]] = db.run(
    commits.filter(_.repoURL === repoUrl).result
  )

  private def getByRepositoryQuery(info: RepoParameters, isValid: Option[Boolean]): Query[Tables.CommitTable, Commit, Seq] = for {
    repo <- filterRepos(info)
    commit <- isValid match {
      case Some(valid) => commits.filter { c => c.repoURL === repo.url && ((c.nrOfTickets > 0) === valid) }
      case None        => commits.filter { _.repoURL === repo.url }
    }
  } yield commit

  private def filterRepos(info: RepoParameters): Query[RepositoryTable, Repository, Seq] = repos.filter { repo => repo.host === info.host && repo.project === info.project && repo.repository === info.repository }
  private def filterCommitById(until: String): Rep[String] => Query[CommitTable, Commit, Seq] = (url: Rep[String]) => commits.filter { c => c.id === until && c.repoURL === url }
  private def filterCommitByDate(until: DateTime): Rep[String] => Query[CommitTable, Commit, Seq] = (url: Rep[String]) => commits.filter { c => c.date === until && c.repoURL === url }

  private def getCommitsByRange(info: RepoParameters, lastCommit: Rep[String] => Query[CommitTable, Commit, Seq], firstCommit: Rep[String] => Query[CommitTable, Commit, Seq], isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    firstCommit <- firstCommit(repo.url)
    lastCommit <- lastCommit(repo.url)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsUntil(info: RepoParameters, targetCommit: Rep[String] => Query[CommitTable, Commit, Seq], isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    firstCommit <- targetCommit(repo.url)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date <= firstCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date <= firstCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsSince(info: RepoParameters, targetCommit: Rep[String] => Query[CommitTable, Commit, Seq], isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    lastCommit <- targetCommit(repo.url)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= lastCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= lastCommit.date && c.repoURL === repo.url }
    }
  } yield result

  def get(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Commit]] = {
    logger.info(s"Get commits - $repo - $filter - $pagination")
    pageQuery(createQuery(repo, filter).sortBy(_.date.desc), pagination)
  }

  def createQuery(repo: RepoParameters, filter: FilterParameters) = filter match {
    case FilterParameters(Some(sinceCommit), Some(untilCommit), _, _, _) =>
      logger.info(s"Query = CommitsByRange(id's) [$sinceCommit] and [$untilCommit]")
      getCommitsByRange(repo, filterCommitById(sinceCommit), filterCommitById(untilCommit), filter.valid)
    case FilterParameters(Some(sinceCommit), None, _, _, _) =>
      logger.info(s"Query = CommitsSince(id) [$sinceCommit]")
      getCommitsSince(repo, filterCommitById(sinceCommit), filter.valid)
    case FilterParameters(_, Some(untilCommit), _, _, _) =>
      logger.info(s"Query = CommitsUntil(id) [$untilCommit]")
      getCommitsUntil(repo, filterCommitById(untilCommit), filter.valid)
    case FilterParameters(_, _, _, Some(sinceCommit), Some(untilCommit)) =>
      logger.info(s"Query = CommitsByRange(dates) [$sinceCommit] and [$untilCommit]")
      getCommitsByRange(repo, filterCommitByDate(sinceCommit), filterCommitByDate(untilCommit), filter.valid)
    case FilterParameters(_, _, _, Some(sinceCommit), _) =>
      logger.info(s"Query = CommitsSince(date) [$sinceCommit]")
      getCommitsSince(repo, filterCommitByDate(sinceCommit), filter.valid)
    case FilterParameters(_, _, _, _, Some(untilCommit)) =>
      logger.info(s"Query = CommitsUntil(date) [$untilCommit]")
      getCommitsUntil(repo, filterCommitByDate(untilCommit), filter.valid)
    case _ =>
      logger.info("Query = ByRepository")
      getByRepositoryQuery(repo, filter.valid)
  }

  def pageQuery[E, T](query: Query[T, E, Seq], pagination: PageParameters): Future[PagedResult[E]] = executeQuery {
    val pagedQuery = pagination match {
      case PageParameters(Some(page), Some(perPage)) if page >= 1 && perPage <= defaultMaxPerPage =>
        logger.info("0 - Both")
        query.drop((page - 1) * perPage).take(perPage)
      case PageParameters(Some(page), None) if page >= 1 =>
        logger.info("1 - PageNumber only")
        query.drop((page - 1) * defaultPerPage).take(defaultPerPage)
      case PageParameters(None, Some(perPage)) if perPage <= defaultMaxPerPage =>
        logger.info("2 - PerPage only")
        query.drop((defaultPageNumber - 1) * perPage).take(perPage)
      case PageParameters(None, None) =>
        logger.info("3 - None")
        query.take(defaultPerPage)
    }
    for {
      result <- pagedQuery.result
      totalCount <- query.length.result
    } yield new PagedResult(result, totalCount)
  }

  private def executeQuery[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = handleError(db.run(action))

  def youngest(repoUrl: String): Future[Option[Commit]] = {
    logger.info(s"Getting youngest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.desc).result.headOption))
  }

  def oldest(repoUrl: String): Future[Option[Commit]] = {
    logger.info(s"Getting oldest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.asc).result.headOption))
  }

  def tickets(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Ticket]] = {
    logger.info(s"Get tickets - $repo - $filter - $pagination")
    val commitQuery = createQuery(repo, filter).sortBy(_.date.desc)
    val future = pageQuery(commitQuery, pagination)
    future.map { result =>
      result match {
        case PagedResult(Nil, totalCount) => PagedResult(Nil, totalCount)
        case PagedResult(items, totalCount) =>
          val r: Seq[List[Ticket]] = items.flatMap { x => x.tickets }
          PagedResult(r.flatten, totalCount)
      }
    }
  }
}
