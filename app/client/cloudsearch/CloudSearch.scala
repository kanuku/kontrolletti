package client.cloudsearch

import scala.concurrent.Future
import client.RequestDispatcher
import javax.inject._
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.ning.NingWSClient
import model._
import utils._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

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
            logger.info("Received http-status:"+result.status)
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

//object Test extends App {
//  import play.api.libs.ws._
//  import play.api.libs.ws.ning._
//  //  val logger: Logger = Logger { this.getClass }
//  println("####### START")
//  import model.KontrollettiToJsonParser._
//  import play.api.libs.ws.WS
//
//  val apps = "search-kontrolletti-apps-govh5a2caavwfgsivobuq6qri4.eu-west-1.cloudsearch.amazonaws.com"
//  val appsDoc = "doc-kontrolletti-apps-govh5a2caavwfgsivobuq6qri4.eu-west-1.cloudsearch.amazonaws.com"
//  import scala.concurrent.ExecutionContext.Implicits.global
//  import play.api.libs.ws.ning._
//  import play.api.libs.ws._
//
//  val client = {
//    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
//    new play.api.libs.ws.ning.NingWSClient(builder.build())
//  }
//  val result = client.url("https://google.com/").get()
//
//  result.onSuccess {
//    case grind =>
//      println("HTTP STATUS" + grind.status)
//      client.close()
//  }
//  println("####### END")
//  //  System.exit(1))))
//
//}