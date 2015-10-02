package dao

import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import model.Author
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.sql.SQLException

trait AuthorRepository {

  def initializeDatabase: Future[Unit]
  def save(authors: List[Author]): Future[Unit]
  def list(): Future[Seq[Author]]
  def authorByEmail(email: String): Future[Option[Author]]

}

@Singleton
class AuthorRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends AuthorRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  val logger: Logger = Logger(this.getClass())
  private val authors = Tables.authors

  def initializeDatabase: Future[Unit] = {
    logger.info("Started initializing table for Authors")
    db.run { authors.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for Authors")
    }
  }

  def save(input: List[Author]): Future[Unit] = {
    logger.info("Saving author")
    handleError( db.run(authors ++= input).map(_ => ()))
  }

  def authorByEmailExists(email: String) =
    authors.filter(_.email === email).exists

  def authorByEmail(email: String): Future[Option[Author]] = db.run(authors.filter(_.email === email).result.headOption)

  def list(): Future[Seq[Author]] = db.run(authors.result)

}

