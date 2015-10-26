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

/**
 * @author fbenjamin
 */
trait CommitRepository {
  val defaultPageNumber = 1
  val defaultMaxPerPage = 500
  def initializeDatabase: Future[Unit]
  def all(): Future[Seq[Commit]]
  def save(commits: List[Commit]): Future[Unit]
  def byId(host: String, project: String, repository: String, id: String): Future[Option[Commit]]
  def get(host: String, project: String, repository: String, since: Option[String] = None, until: Option[String] = None, pageNumber: Int = defaultPageNumber, maxPerPage: Int = defaultMaxPerPage, valid: Option[Boolean] = None): Future[Seq[Commit]]
  def youngest(repoUrl: String): Future[Option[Commit]]
  def oldest(repoUrl: String): Future[Option[Commit]]

}

@Singleton
class CommitRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommitRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  private val queries = Queries
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
      case Some(valid) => commits.filter { c => c.repoURL === repo.url && c.isValid === valid }
      case None        => commits.filter { _.repoURL === repo.url }
    }
  } yield commit

  private def getCommitsByRange(host: String, project: String, repository: String, since: String, until: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    firstCommit <- commits.filter { c => c.id === since && c.repoURL === repo.url }
    lastCommit <- commits.filter { c => c.id === until && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && c.isValid === valid && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= lastCommit.date && c.date <= firstCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsUntilCommitDate(host: String, project: String, repository: String, until: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    targetCommit <- commits.filter { c => c.id === until && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date <= targetCommit.date && c.isValid === valid && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date <= targetCommit.date && c.repoURL === repo.url }
    }
  } yield result

  private def getCommitsSinceCommitDate(host: String, project: String, repository: String, since: String, isValid: Option[Boolean]) = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    targetCommit <- commits.filter { c => c.id === since && c.repoURL === repo.url }
    result <- isValid match {
      case Some(valid) => commits.filter { c => c.date >= targetCommit.date && c.isValid === valid && c.repoURL === repo.url }
      case None        => commits.filter { c => c.date >= targetCommit.date && c.repoURL === repo.url }
    }
  } yield result

  def get(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNumber: Int, maxPerPage: Int, valid: Option[Boolean]): Future[Seq[Commit]] = {
    (since, until) match {
      case (Some(sinceCommit), Some(untilCommit)) =>
        logger.info(s" 0 -Get  from $host/$project/$repository since $since until $until, page number $pageNumber limit $maxPerPage and valid $valid")
        pageQuery(getCommitsByRange(host, project, repository, sinceCommit, untilCommit, valid).sortBy(_.date.desc), pageNumber: Int, maxPerPage: Int)
      case (Some(sinceCommit), None) =>
        logger.info(s" 1 -Get from $host/$project/$repository since $since until $until, page number $pageNumber limit $maxPerPage and valid $valid")
        pageQuery(getCommitsSinceCommitDate(host, project, repository, sinceCommit, valid).sortBy(_.date.desc), pageNumber: Int, maxPerPage: Int)
      case (None, Some(untilCommit)) =>
        logger.info(s" 2 -Get from $host/$project/$repository since $since until $until, page number $pageNumber limit $maxPerPage and valid $valid")
        pageQuery(getCommitsUntilCommitDate(host, project, repository, untilCommit, valid).sortBy(_.date.desc), pageNumber: Int, maxPerPage: Int)
      case _ =>
        logger.info(s" 3 -Get from $host/$project/$repository since $since until $until, page number $pageNumber limit $maxPerPage and valid $valid")
        pageQuery(getByRepositoryQuery(host, project, repository, valid).sortBy(_.date.desc), pageNumber: Int, maxPerPage: Int)
    }
  }

  def pageQuery(query: Query[Tables.CommitTable, Commit, Seq], pageNumber: Int, maxPerPage: Int) = {
    if (pageNumber > 1)
      handleError(db.run(query.drop((pageNumber - 1) * maxPerPage).take(maxPerPage).result))
    else
      handleError(db.run(query.take(maxPerPage).result))
  }

  def youngest(repoUrl: String): Future[Option[Commit]] = {
    logger.debug(s"Getting youngest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.desc).result.headOption))
  }

  def oldest(repoUrl: String): Future[Option[Commit]] = {
    logger.debug(s"Getting oldest commit for $repoUrl")
    handleError(db.run(commits.filter { x => x.repoURL === repoUrl }.sortBy(_.date.asc).result.headOption))
  }

}
