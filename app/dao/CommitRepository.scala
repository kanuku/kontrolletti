package dao

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import org.joda.time.DateTime
import dao.KontrollettiPostgresDriver.api._
import javax.inject.Inject
import javax.inject.Singleton
import model.Commit
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.db.slick.HasDatabaseConfigProvider
import utility.FutureUtil
import play.api.libs.json.JsValue
import slick.jdbc.GetResult
import model.KontrollettiToModelParser
import play.api.libs.json.Json
import model.Ticket

/**
 * @author fbenjamin
 */
trait CommitRepository {
  val defaultPageNumber = 1
  val defaultMaxPerPage = 500
  val defaultPerPage = 50
  def initializeDatabase: Future[Unit]

  def all(): Future[Seq[Commit]]

  def save(commits: List[Commit]): Future[Unit]
  def byId(host: String, project: String, repository: String, id: String): Future[Option[Commit]]
  def get(host: String, project: String, repository: String, since: Option[String] = None, until: Option[String] = None, pageNumber: Option[Int] = Option(defaultPageNumber), perPage: Option[Int] = Option(defaultPerPage), valid: Option[Boolean] = None): Future[Seq[Commit]]
  def youngest(repoUrl: String): Future[Option[Commit]]
  def oldest(repoUrl: String): Future[Option[Commit]]
  def tickets(host: String, project: String, repository: String, since: Option[String] = None, until: Option[String] = None, pageNumber: Option[Int] = Option(defaultPageNumber), perPage: Option[Int] = Option(defaultPerPage), valid: Option[Boolean] = Some(true)): Future[Seq[Ticket]]

}

@Singleton
class CommitRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommitRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  private val logger: Logger = Logger(this.getClass())
  private val commits = Tables.commits
  private val repos = Tables.repositories

  def initializeDatabase = {
    logger.info("Started initializing table for Commit")
    db.run { commits.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for Commit")
    }
  }

  def all(): Future[Seq[Commit]] = handleError(db.run(commits.result))

  def save(input: List[Commit]): Future[Unit] = {
    logger.info("saving " + input.size + " commits")
    handleError(db.run(commits ++= input).map(_ => ()))
  }

  def byId(host: String, project: String, repository: String, id: String): Future[Option[Commit]] = db.run {
    getByRepositoryQuery(host, project, repository, None).filter(_.id === id).result.headOption
  }

  private def getByRepositoryQuery(host: String, project: String, repository: String, isValid: Option[Boolean]): Query[Tables.CommitTable, Commit, Seq] = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    commit <- isValid match {
      case Some(valid) => commits.filter { c => c.repoURL === repo.url && ((c.nrOfTickets > 0) === valid) }
      case None        => commits.filter { _.repoURL === repo.url }
    }
  } yield commit

  private def getCommitsByRange(host: String, project: String, repository: String, since: String, until: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    firstCommit <- commits.filter { c => c.id === until && c.repoURL === repo.url }
    lastCommit <- commits.filter { c => c.id === since && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsUntilCommitDate(host: String, project: String, repository: String, until: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    targetCommit <- commits.filter { c => c.id === until && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date <= targetCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date <= targetCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsSinceCommitDate(host: String, project: String, repository: String, since: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    targetCommit <- commits.filter { c => c.id === since && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= targetCommit.date && ((c.nrOfTickets > 0) === valid) && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= targetCommit.date && c.repoURL === repo.url }
    }
  } yield result

  def get(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNumber: Option[Int], perPage: Option[Int], valid: Option[Boolean]): Future[Seq[Commit]] = {
    logger.info(s"Get  from $host/$project/$repository since $since until $until, page number $pageNumber limit $perPage and valid $valid")
    (since, until) match {
      case (Some(sinceCommit), Some(untilCommit)) =>
        pageQuery(getCommitsByRange(host, project, repository, sinceCommit, untilCommit, valid).sortBy(_.date.desc), pageNumber, perPage)
      case (Some(sinceCommit), None) =>
        pageQuery(getCommitsSinceCommitDate(host, project, repository, sinceCommit, valid).sortBy(_.date.desc), pageNumber, perPage)
      case (None, Some(untilCommit)) =>
        pageQuery(getCommitsUntilCommitDate(host, project, repository, untilCommit, valid).sortBy(_.date.desc), pageNumber, perPage)
      case _ =>
        pageQuery(getByRepositoryQuery(host, project, repository, valid).sortBy(_.date.desc), pageNumber, perPage)
    }
  }

  def pageQuery(query: Query[Tables.CommitTable, Commit, Seq], pageNumber: Option[Int], perPage: Option[Int]) = {
    logger.info(s"Query pagination -page $pageNumber, perPage $perPage")
    (pageNumber, perPage) match {
      case (Some(page), Some(perPage)) if page >= 1 && perPage <= defaultMaxPerPage =>
        logger.info("0")
        handleError(db.run(query.drop((page - 1) * perPage).take(perPage).result))
      case (Some(page), None) if page >= 1 =>
        logger.info("1")
        handleError(db.run(query.drop((page - 1) * defaultPerPage).take(defaultPerPage).result))
      case (None, Some(perPage)) if perPage <= defaultMaxPerPage =>
        logger.info("2")
        handleError(db.run(query.drop((defaultPageNumber - 1) * perPage).take(perPage).result))
      case (_, _) =>
        logger.info("3")
        handleError(db.run(query.take(defaultPerPage).result))
    }
  }

  def youngest(repoUrl: String): Future[Option[Commit]] = {
    logger.debug(s"Getting youngest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.desc).result.headOption))
  }

  def oldest(repoUrl: String): Future[Option[Commit]] = {
    logger.info(s"Getting oldest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.asc).result.headOption))
  }

  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNumber: Option[Int], perPage: Option[Int], valid: Option[Boolean]): Future[Seq[Ticket]] =
    get(host, project, repository, since, until, pageNumber, perPage, valid).map { commits =>
      (for {
        commit <- commits.toList
        tickets <- commit.tickets
      } yield tickets).flatten.toSeq
    }
}
