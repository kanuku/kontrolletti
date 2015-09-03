package dao

import scala.concurrent.Future
import com.google.inject.Inject
import com.google.inject.Singleton
import model.AppInfo
import model.Commit
import model.Ticket
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf
import play.api.Play

trait DataStoreDAO { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class AppInfos(tag: Tag) extends Table[AppInfo](tag, "APP_INFO") {
    def scmUrl = column[String]("SCM_URL", O.PrimaryKey)
    def documentationUrl = column[String]("DOC_URL")
    def specificationUrl = column[String]("SPEC_URL")
    def lastModified = column[String]("LAST_MODIFIED")

    def * = (scmUrl, documentationUrl, specificationUrl, lastModified) <> (AppInfo.tupled, AppInfo.unapply _)

  }
  /**
   * Saves the given AppInfo documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   *
   * @return A Future with true if the result of the action was successful.
   */
  def saveApps(input: Seq[AppInfo]): Future[Unit]

  /**
   * Saves the given Commit-documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   *
   * @return A future with true if the result of the action was successful.
   */
  def saveCommits(app: AppInfo, input: Seq[Commit]): Future[Boolean]

  /**
   * Saves the given Ticket documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   *
   * @return A future with true if the result of the action was successful.
   */
  def saveTickets(app: AppInfo, input: Seq[Ticket]): Future[Boolean]

  /**
   * Retrieves a list of AppInfos.
   * @return A future with the AppInfoResponse
   */
  def appInfos(): Future[Seq[AppInfo]]

  /**
   *
   * Retrieves a list of AppInfos by their id's.
   * @param ids Set of id's to retrieve
   * @return A future with the AppInfoResponse
   */
  def appInfos(ids: Set[String]): Future[Seq[AppInfo]]

  /**
   * Retrieves a list of commits.
   * @param scmUrl
   * @param size
   * @param start
   * @param since CommitId from where to start looking for commits
   * @param until Until commits from this commit
   *
   * @return A future with the CommitResponse
   */
  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Commit]]

  /**
   * Retrieves a single commit.
   * @return A future with the CommitResponse
   */
  def commit(scmUrl: String, id: String): Future[Commit]

  /**
   * Retrieves a list of tickets.
   * @param scmUrl
   * @param size
   * @param start
   * @param since CommitId from where to start looking for commits
   * @param until Until commits from this commit
   *
   * @return A future with the TicketResponse
   */
  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Ticket]]

  /**
   * Retrieves a single ticket.
   * @return A future with the TicketResponse
   */
  def ticket(scmUrl: String, id: String): Future[Ticket]
}

@Singleton
class DataStoreDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DataStoreDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import Play.current
  import driver.api._

  val apps = TableQuery[AppInfos]
  val logger: Logger = Logger { this.getClass }

  def saveApps(input: Seq[AppInfo]) = {

    logger.info(s"Apps to save:" + input.size)
    db.run(apps ++= input).map(_ => ())
  }

  def appInfos(ids: Set[String]): Future[Seq[AppInfo]] = ???

  def appInfos(): Future[Seq[AppInfo]] = {
    logger.info("Searching")
    val result = db.run(apps.result)
    result
  }


  def saveCommits(app: AppInfo, input: Seq[Commit]): Future[Boolean] = ???

  def saveTickets(app: AppInfo, input: Seq[Ticket]): Future[Boolean] = ???

  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Commit]] = ???

  def commit(scmUrl: String, id: String): Future[Commit] = ???

  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Ticket]] = ???

  def ticket(scmUrl: String, id: String): Future[Ticket] = ???
}

 