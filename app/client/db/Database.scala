package client.db

import model.AppInfo
import model.Commit
import scala.concurrent.Future
import model.Ticket

/**
 * @author fbenjamin
 */
trait Database {

  /**
   * Saves the given AppInfo documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   *
   * @return A Future with true if the result of the action was successful.
   */
  def saveAppInfos(input: List[AppInfo]): Future[Boolean]

  /**
   * Saves the given Commit-documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   *
   * @return A future with true if the result of the action was successful.
   */
  def saveCommits(app: AppInfo, input: List[Commit]): Future[Boolean]

  /**
   * Saves the given Ticket documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   *
   * @return A future with true if the result of the action was successful.
   */
  def saveTickets(app: AppInfo, input: List[Ticket]): Future[Boolean]

  /**
   * Retrieves a list of AppInfos.
   * @return A future with the AppInfoResponse
   */
  def appInfos(): Future[Option[ List[AppInfo]]]

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
  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Option[List[Commit]]]

  /**
   * Retrieves a single commit.
   * @return A future with the CommitResponse
   */
  def commit(scmUrl: String, id: String):Future[Option[Commit]]

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
  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Option[List[Ticket]]]

  /**
   * Retrieves a single ticket.
   * @return A future with the TicketResponse
   */
  def ticket(scmUrl: String, id: String): Future[Option[Ticket]]

}

class DabaseImpl extends Database {
  
  def saveAppInfos(input: List[AppInfo]): Future[Boolean] = ???

  def saveCommits(app: AppInfo, input: List[Commit]): Future[Boolean] = ???

  def saveTickets(app: AppInfo, input: List[Ticket]): Future[Boolean] = ???

  def appInfos(): Future[Option[ List[AppInfo]]] = ???

  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Option[List[Commit]]] = ???

  def commit(scmUrl: String, id: String):Future[Option[Commit]] = ???

  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[Option[List[Ticket]]] = ???

  def ticket(scmUrl: String, id: String): Future[Option[Ticket]] = ???

}
