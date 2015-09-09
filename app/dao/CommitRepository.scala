package dao

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.db.slick.HasDatabaseConfigProvider
import slick.codegen.SourceCodeGenerator
import slick.driver.PostgresDriver
import play.api.Logger
import model.Commit
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Writes
/**
 * @author fbenjamin
 */
trait CommitRepository { self: HasDatabaseConfig[KontrollettiPostgresDriver] =>

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Option[List[Commit]]]
  def commit(host: String, project: String, repository: String, id: String): Future[Option[List[Commit]]]

  import driver.api._

  def initializeDatabase: Future[Unit]

  class CommitsTable(tag: Tag)(implicit reader: Reads[Commit]) extends Table[TableDefinitionKey1[Commit]](tag, "COMMITS") {
    def id = column[String]("id", O.PrimaryKey)
    def json = column[JsValue]("json")

    def * = (id, json) <> ((apply[Commit] _).tupled, TableDefinitionKey1.unapply[Commit])

    def apply[T](_id: String, _json: JsValue)(implicit reader: Reads[T]) = new TableDefinitionKey1[T](_id, _json)
    def unapply[T](table: TableDefinitionKey1[T]): Option[(String, JsValue)] = Some((table.id1, table.jsonValue))
  }

}

@Singleton
class CommitRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends CommitRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  val logger: Logger = Logger(this.getClass())
  import driver.api._
  import model.KontrollettiToModelParser.commitReader
  private val commits = TableQuery[CommitsTable]

  def initializeDatabase = {
    logger.info("Started initializing table for Commit")
    db.run { commits.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for Commit")
    }

  }
  

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Option[List[Commit]]] = ???
  def commit(host: String, project: String, repository: String, id: String): Future[Option[List[Commit]]] = ???

}
