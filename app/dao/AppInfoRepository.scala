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
import model.AppInfo

trait AppInfoRepository { self: HasDatabaseConfig[KontrollettiPostgresDriver] =>
  import driver.api._

  def initializeDatabase: Future[Unit]

  def save(app: List[AppInfo]): Future[Unit]
  def list(): Future[Seq[AppInfo]]

  class AppsTable(tag: Tag) extends Table[AppInfo](tag, "APP_INFOS") {
    def scmUrl = column[String]("scm_url", O.PrimaryKey)
    def docUrl = column[Option[String]]("doc_url")
    def specUrl = column[Option[String]]("spec_url")
    def lastModified = column[Option[String]]("last_modified")

    def * = (scmUrl, docUrl, specUrl, lastModified) <> ((AppInfo.apply _).tupled, AppInfo.unapply)
  }
}

@Singleton
class AppInfoRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AppInfoRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  val logger: Logger = Logger(this.getClass())
  import driver.api._

  private val apps = TableQuery[AppsTable]

  def initializeDatabase = {
    logger.info("Started initializing table for AppInfo")
    db.run { apps.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for AppInfo")
    }

  }

  def save(input: List[AppInfo]): Future[Unit] = {
    logger.info(s"AppInfo's to save: $input")
    db.run(apps ++= input).map(_ => ())
  }

  def list(): Future[Seq[AppInfo]] = db.run(apps.result)
}