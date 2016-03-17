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

  def all(): Future[Seq[Commit]]
  def save(commits: List[Commit]): Future[Unit]
  def byId(info: RepoParameters, id: String): Future[Option[Commit]]
  def get(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Commit]]
  def youngest(repoUrl: String): Future[Option[Commit]]
  def oldest(repoUrl: String): Future[Option[Commit]]
  def tickets(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Ticket]]

}

@Singleton
class CommitRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommitRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  private val logger: Logger = Logger(this.getClass)
  private val commits = Tables.commits
  private val repos = Tables.repositories
  private val defaultPageNumber = 1
  private val defaultMaxPerPage = 500
  private val defaultPerPage = 50


  def all(): Future[Seq[Commit]] = handleError(db.run(commits.result))

  def save(input: List[Commit]): Future[Unit] = {
    logger.info("saving " + input.size + " commits")
    handleError(db.run(commits ++= input).map(_ => ()))
  }

  def byId(info: RepoParameters, id: String): Future[Option[Commit]] = db.run {
    getByRepositoryQuery(info, None).filter(_.id === id).result.headOption
  }

  private def getByRepositoryQuery(info: RepoParameters, isValid: Option[Boolean]): Query[Tables.CommitTable, Commit, Seq] = for {
    repo <- filterRepos(info)
    commit <- isValid match {
      case Some(valid) => commits.filter { c => c.repoURL === repo.url && ((c.nrOfTickets > 0) === valid) }
      case None        => commits.filter { _.repoURL === repo.url }
    }
  } yield commit

  private def filterRepos(info: RepoParameters): Query[RepositoryTable, Repository, Seq] = repos.filter { repo => repo.host === info.host && repo.project === info.project && repo.repository === info.repository }
  private def getCommitsByRange(info: RepoParameters, sinceDate: DateTime, untilDate: DateTime, isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= sinceDate && c.date <= untilDate && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= sinceDate && c.date <= untilDate && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsUntil(info: RepoParameters, untilDate: DateTime, isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date <= untilDate && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date <= untilDate && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsSince(info: RepoParameters, sinceDate: DateTime, isValid: Option[Boolean]) = for {
    repo <- filterRepos(info)
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= sinceDate && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= sinceDate && c.repoURL === repo.url }
    }
  } yield result

  private def toDateFilterParameter(repo: RepoParameters, filter: FilterParameters): Future[DateFilterParams] = filter match {
    case FilterParameters(Some(sinceId), Some(untilId), valid, _, _) =>
      for {
        sinceOpt <- byId(repo, sinceId)
        untilOpt <- byId(repo, untilId)
      } yield DateFilterParams(sinceOpt.map(_.date), untilOpt.map(_.date), valid)
    case FilterParameters(Some(sinceId), None, valid, _, _) =>
      for {
        sinceOpt <- byId(repo, sinceId)
      } yield DateFilterParams(sinceOpt.map(_.date), None, valid)
    case FilterParameters(None, Some(untilId), valid, _, _) =>
      for {
        untilOpt <- byId(repo, untilId)
      } yield DateFilterParams(None, untilOpt.map(_.date), valid)
    case FilterParameters(_, _, valid, sinceDateOpt, untilDateOpt) =>
      Future.successful(DateFilterParams(sinceDateOpt, untilDateOpt, valid))
  }

  /** FIXME: in order to make it really correct, api needs to be evolved
    * so that query by date will generate a new link based on commit id for pagination
    */
  def get(repo: RepoParameters, filter: FilterParameters, pagination: PageParameters): Future[PagedResult[Commit]] = {
    logger.info(s"Get commits - $repo - $filter - $pagination")
    val dateFilterParamsFuture = toDateFilterParameter(repo, filter)
    dateFilterParamsFuture flatMap { dateFilterParms =>
      pageQuery(createQuery(repo, dateFilterParms).sortBy(_.date.desc), pagination)
    }
  }

  def createQuery(repo: RepoParameters, filter: DateFilterParams) = filter match {
    case DateFilterParams(Some(sinceDate), Some(untilDate), valid) =>
      getCommitsByRange(repo, sinceDate, untilDate, valid)
    case DateFilterParams(Some(sinceDate), None, valid) =>
      getCommitsSince(repo, sinceDate, valid)
    case DateFilterParams(None, Some(untilDate), valid) =>
      getCommitsUntil(repo, untilDate, valid)
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
    val dateFilterParamsFuture = toDateFilterParameter(repo, filter)
    val future = dateFilterParamsFuture flatMap { dateFilterParams =>
      val commitQuery = createQuery(repo, dateFilterParams).sortBy(_.date.desc)
      pageQuery(commitQuery, pagination)
    }
    future.map {
      case PagedResult(Nil, totalCount) => PagedResult(Nil, totalCount)
      case PagedResult(items, totalCount) =>
        val r: Seq[List[Ticket]] = items.flatMap { x => x.tickets }
        PagedResult(r.flatten, totalCount)
    }
  }
}
