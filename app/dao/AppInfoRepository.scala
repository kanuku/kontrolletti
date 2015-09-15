package dao

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
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.sql.SQLException
import org.joda.time.DateTime



trait AppInfoRepository { self: HasDatabaseConfig[KontrollettiPostgresDriver] =>
  import driver.api._

  def initializeDatabase: Future[Unit]

  def saveApps(apps: List[AppInfo]): Future[Unit]
  
  def scmUrls():Future[List[String]]
  
  
  def list(): Future[Seq[AppInfo]]

  class AppsTable(tag: Tag) extends Table[AppInfo](tag, "APP_INFOS") {
    def scmUrl = column[String]("scm_url", O.PrimaryKey)
    def docUrl = column[Option[String]]("doc_url")
    def specUrl = column[Option[String]]("spec_url")
    def lastModified = column[DateTime]("last_modified")
    def * = (scmUrl, docUrl, specUrl, lastModified) <> (AppInfo.tupled, AppInfo.unapply)
  }
}

@Singleton
class AppInfoRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends AppInfoRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {

  val logger: Logger = Logger(this.getClass())

  import driver.api._

  private val apps = TableQuery[AppsTable]

  def initializeDatabase = {
    logger.info("Started initializing table for AppInfo")
    db.run { apps.schema.create }.map { x =>
      logger.info("Fisnihed initializing table for AppInfo")
    }
  }

  def saveApps(input: List[AppInfo]): Future[Unit] = {
    logger.info(s"Number of AppInfo's to save:" + input.size)
    val res = db.run(apps ++= input).map(_ => ())
    res recoverWith {
      case ex: SQLException =>
        logger.error(ex.getNextException.getMessage)
        logger.error(ex.getMessage)
        Future.failed(new Exception("DATABASE OPERATION(SAVE) FAILED"))
    }
    res
  }
  def scmUrls:Future[List[String]] = {
    list().map { _.map { x => x.scmUrl }.toList }
  }

  def list(): Future[Seq[AppInfo]] = db.run(apps.result)
}