package dao

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.google.inject.ImplementedBy
import javax.inject._
import model.AppInfo
import model.Commit
import model.Ticket
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import slick.lifted.TableQuery
import slick.driver.JdbcProfile
import play.api.Play


trait DataStoreDAO { 

  /**
   * Saves the given AppInfo documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   *
   * @return A Future with true if the result of the action was successful.
   */
  def saveAppInfos(input: Seq[AppInfo])

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
class DataStoreDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends DataStoreDAO{ // with HasDatabaseConfigProvider[MyPostgresDriver] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
//  val apps = TableQuery[AppInfos]

  def saveAppInfos(input: Seq[AppInfo]) = ??? // db.run(this.apps ++= input).map(_ => ())

  def appInfos(ids: Set[String]): Future[Seq[AppInfo]] = ???
  
  
  def appInfos(): Future[Seq[AppInfo]] = ???

  def saveCommits(app: AppInfo, input: Seq[Commit]): Future[Boolean] = ???

  def saveTickets(app: AppInfo, input: Seq[Ticket]): Future[Boolean] = ???

  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Commit]] = ???

  def commit(scmUrl: String, id: String): Future[Commit] = ???

  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Seq[Ticket]] = ???

  def ticket(scmUrl: String, id: String): Future[Ticket] = ???
}
