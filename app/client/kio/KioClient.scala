package client.kio

import scala.concurrent.Future

import client.RequestDispatcher
import client.oauth.OAuthAccessToken
import javax.inject.Inject
import javax.inject.Singleton
import model.AppInfo
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse
import utility.Transformer

/**
 * @author fbenjamin
 */
trait KioClient {

  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]]

}

object KioParser {

  implicit val appInfoReader: Reads[AppInfo] = (
    (JsPath \ "scm_url").read[String] and
    (JsPath \ "service_url").read[String] and
    (JsPath \ "created").read[String] and
    (JsPath \ "last_modified").read[String])(AppInfo.apply _)

}

@Singleton
class KioClientImpl @Inject() (dispatcher: RequestDispatcher, config: KioClientConfiguration) extends KioClient {
  private val logger: Logger = Logger { this.getClass }
  private val transformer = Transformer
  import KioParser._
  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]] = {
    dispatcher.requestHolder(config.serviceUrl) //
      .withHeaders(("Authorization", "Bearer " + accessToken.accessToken)).get()
      .flatMap { response =>
        logger.info("Received http-status-code: " + response.status)
        transformer.transform[List[AppInfo]](response.body)
      }
  }

}

