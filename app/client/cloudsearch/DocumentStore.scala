package client.cloudsearch

import scala.concurrent.Future
import client.RequestDispatcher
import javax.inject._
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import model._
import utility.Transformer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import model.KontrollettiToJsonParser._
case class CloudSearchException(msg: String) extends Exception(msg)
case class UploadRequest[T](id: String, operation: String, document: T)
case class SearchResquest(query: String)
case class AppInfoResponse(found: Int, start: Int, result: Option[List[AppInfo]])
case class CommitResponse(found: Int, start: Int, result: Option[List[Commit]])
case class TicketResponse(found: Int, start: Int, result: Option[List[Ticket]])

/**
 * @author fbenjamin
 */
trait DocumentStore {

  /**
   * Saves the given AppInfo documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @return A Future with true if the result of the action was successfull.
   */
  def saveAppInfos(input: List[AppInfo]): Future[Boolean]

  /**
   * Saves the given Commit-documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   * @return A future with true if the result of the action was successfull.
   */
  def saveCommits(app: AppInfo, input: List[Commit]): Future[Boolean]

  /**
   * Saves the given Ticket documents to the document-store.
   *
   * @param input The documents to be added to the document-store.
   * @param app The application where the documents belong to.
   * @return A future with true if the result of the action was successfull.
   */
  def saveTickets(app: AppInfo, input: List[Ticket]): Future[Boolean]

  /**
   * Retrieves a list of AppInfos.
   */
  def appInfos(): Future[AppInfoResponse]
  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[CommitResponse]
  def commit(scmUrl: String, id: String): Future[Commit]
  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[TicketResponse]
  def ticket(scmUrl: String, id: String): Future[TicketResponse]

}

@Singleton
class CloudSearchImpl @Inject() (config: CloudSearchConfiguration, dispatcher: RequestDispatcher) extends DocumentStore {

  private val logger: Logger = Logger { this.getClass }
  private val addOperation = "add"
  private val deleteOperation = "delete"
  private val queryAllAppInfos: String = "*.*"

  def saveAppInfos(input: List[AppInfo]): Future[Boolean] = for {
    url <- config.appsDocEndpoint
    result <- uploadDocuments(url, input)
  } yield result

  //FIXME: Make those implicit declarations go away!!
  def saveCommits(app: AppInfo, input: List[Commit]): Future[Boolean] = {
    import model.KontrollettiToModelParser._
    implicit val appInfo = commit2Id(app)
    for {
      url <- config.commitsDocEndpoint
      result <- uploadDocuments(url, input)
    } yield result
  }

  def saveTickets(app: AppInfo, input: List[Ticket]): Future[Boolean] = ???

  def appInfos(): Future[AppInfoResponse] = for {
    url <- config.appsSearchEndpoint
    result <- documents[AppInfoResponse](url, queryAllAppInfos)
  } yield result

  def commits(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[CommitResponse] = ???
  def commit(scmUrl: String, id: String): Future[Commit] = ???
  def tickets(scmUrl: String, size: Option[Int], start: Option[Int], since: Option[String], untill: Option[String]): Future[TicketResponse] = ???
  def ticket(scmUrl: String, id: String): Future[TicketResponse] = ???

  private def uploadDocuments[T](url: String, docs: List[T])(implicit reader: Format[T], transformer: IdTransformer[T, String]): Future[Boolean] = {
    logger.info(s"Uploading documents to $url")
    for {
      cloudSearchDocuments <- transform2FutureUploadRequest(docs, addOperation)
      response <- dispatcher.requestHolder(url) //
        .withHeaders("Content-Type" -> "application/json") //
        .post(Json.toJson(cloudSearchDocuments))
    } yield (response.status == 200)
  }

  private def documents[T](endpoint: String, query: String)(implicit reader: Reads[T]): Future[T] = for {
    response <- dispatcher.requestHolder(s"http://$endpoint/2013-01-01/search") //
      .withQueryString("q" -> query)
      .get
    result <- Transformer.transform[T](response.body)
  } yield result
}


