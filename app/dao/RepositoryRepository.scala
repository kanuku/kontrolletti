package dao

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import model.Repository
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import dao.KontrollettiPostgresDriver.api._
import utility.FutureUtil._

/**
 * @author fbenjamin
 */
trait RepoRepository {

  def initializeDatabase: Future[Unit]
  def save(repos: List[Repository]): Future[Unit]
  def enabled(): Future[Seq[Repository]]
  def byParameters(host: String, project: String, repository: String): Future[Option[Repository]]
  def all(): Future[Seq[Repository]]

}

@Singleton
class RepoRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends RepoRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {

  val logger: Logger = Logger(this.getClass())

  private val repos = Tables.repositories

  def initializeDatabase = {
    logger.info("Started initializing table for Repositories")
    handleError(db.run { repos.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for Repositories")
    })
  }

  def save(input: List[Repository]): Future[Unit] = {
    logger.info(s"Number of AppInfo's to save:" + input.size)
    handleError(db.run(repos ++= input).map(_ => ()))
  }
  def enabled(): Future[Seq[Repository]] = {
    logger.info("Getting enabled Repositories")
    handleError(db.run(repos.filter { x => x.synch === true }.result))
  }

  def byParameters(host: String, project: String, repository: String): Future[Option[Repository]] = {
    handleError(db.run(repos.filter { repo => repo.host === host && repo.project === project && repo.repository === repository }.result.headOption))
  }

  def all(): Future[Seq[Repository]] = handleError(db.run(repos.result))

}
