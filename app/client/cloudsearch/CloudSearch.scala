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

/**
 * @author fbenjamin
 */
trait CloudSearch {

  def uploadAppInfos(apps: List[AppInfo]): Future[Boolean]

}

case class CloudSearchException(msg: String) extends Exception(msg)
case class UploadDocument[T](id: String, operation: String, document: T)

@Singleton
class CloudSearchImpl @Inject() (config: CloudSearchConfiguration, dispatcher: RequestDispatcher) extends CloudSearch {
  private val addOperation = "add"
  private val deleteOperation = "delete"
  private val endpointConfigEx = new CloudSearchException("""
      Failed to initialize CloudSearch endpoint  
      from configuration! Check the logs for details!
    """)
  private val logger: Logger = Logger { this.getClass }

  def uploadAppInfos(apps: List[AppInfo]): Future[Boolean] = uploadDocuments(config.appsEndpoint, apps)

  def uploadDocuments[T](url: Option[String], docs: List[T])(implicit reader: Format[T], transformer: IdTransformer[T, String]): Future[Boolean] = {
    url match {
      case Some(url) =>
        logger.info(s"Uploading documents to $url")
        val cloudSearchDocuments = transform(docs, addOperation)
        dispatcher.requestHolder(url).withHeaders("Content-Type" -> "application/json") //
          .post(Json.toJson(cloudSearchDocuments)).map { result =>
            logger.info("Received http-status:" + result.status)
            result match {
              case response if (response.status == 200) =>
                true
              case _ =>
                false

            }

          }

      case None => Future.failed(endpointConfigEx)
    }
  }

  private def uploadDocs(url: String) = {

  }

}
 