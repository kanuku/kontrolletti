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

  def initializeDatabase: Future[Unit]
  def all(): Future[Seq[Commit]]
  def save(commits: List[Commit]): Future[Unit]
  def byId(host: String, project: String, repository: String, id: String): Future[Option[Commit]]
  def get(host: String, project: String, repository: String, since: Option[String] = None, until: Option[String] = None, pageNumber: Int = 1, maxPerPage: Int = 100): Future[Seq[Commit]]
  def youngest(repoUrl: String): Future[Option[Commit]]
  def oldest(repoUrl: String): Future[Option[Commit]]

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
    getByRepositoryQuery(host, project, repository).filter(_.id === id).result.headOption
  }

  private def getByRepositoryQuery(host: String, project: String, repository: String): Query[Tables.CommitTable, Commit, Seq] = for {
    repo <- repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }
    commit <- commits.filter { _.repoURL === repo.url }
  } yield commit

  private def getWithSinceAndUntilQuery(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNumber: Int, maxPerPage: Int): Future[Seq[Commit]] = {
    logger.info(s"WithSinceAndUntilQuery- host($host) - project($project) - repository($repository) - since($since) - untill($until) pageNumber($pageNumber) - maxPerPage($maxPerPage)")
    val Some(sinceId) = since
    val Some(untilId) = until
    import slick.jdbc.{ StaticQuery => Q, GetResult }
    implicit val getCommits = GetResult[Commit](r => utility.Transformer.deserialize(Json.parse(r.nextString()))(KontrollettiToModelParser.commitReader))
    val query = sql"""
                        WITH RECURSIVE PARENTS(parent_id, id, date) AS (
                          WITH ALL_COMMITS(parent_id, id, date) AS (
                            SELECT unnest(parent_ids)as parent_id, id, date
                              FROM kont_data."COMMITS" C, kont_data."REPOSITORIES" R
                              WHERE R.host = $host
                              AND R.project = $project
                              AND R.repository = $repository
                              AND C.repository_url = R.URL
                          ), LAST_COMMIT(parent_id, id, date) AS (
                            SELECT parent_id, id, date
                              FROM ALL_COMMITS END_PARENT
                              WHERE id = $untilId
                          )
                          SELECT START_CHILD.parent_id, START_CHILD.id, START_CHILD.date
                            FROM ALL_COMMITS AS START_CHILD , LAST_COMMIT LC
                            WHERE START_CHILD.id = $sinceId
                          UNION ALL
                          SELECT   P.parent_id, P.id, P.date
                            FROM ALL_COMMITS P,PARENTS AS ND, LAST_COMMIT AS LAST
                            WHERE P.id = ND.parent_id
                            AND P.date >= LAST.date
                        )
                        SELECT C.json_value
                        FROM PARENTS P, kont_data."COMMITS" C
                        WHERE C.id = P.id
                        ORDER BY C.date

                        ;
                  """.as[Commit]
    db.run(query)
  }

  def get(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNumber: Int, maxPerPage: Int): Future[Seq[Commit]] = {

    (since, until) match {
      case (None, None)                       => pageQuery(getByRepositoryQuery(host, project, repository).sortBy(_.date.desc).take(maxPerPage), pageNumber: Int, maxPerPage: Int)
      case (Some(sinceDate), None)            => pageQuery(getByRepositoryQuery(host, project, repository).sortBy(_.date.desc).take(maxPerPage), pageNumber: Int, maxPerPage: Int)
      case (None, Some(untilDate))            => pageQuery(getByRepositoryQuery(host, project, repository).sortBy(_.date.desc).take(maxPerPage), pageNumber: Int, maxPerPage: Int)
      case (Some(sinceDate), Some(untilDate)) => getWithSinceAndUntilQuery(host, project, repository, since, until, pageNumber, maxPerPage)
      //

    }
  }

  def pageQuery(query: Query[Tables.CommitTable, Commit, Seq], pageNumber: Int, maxPerPage: Int) = {
    if (pageNumber > 1)
      handleError(db.run(query.drop((pageNumber - 1) * maxPerPage).result))
    else
      handleError(db.run(query.result))
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
