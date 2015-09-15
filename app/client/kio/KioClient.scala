package client.kio

import scala.concurrent.Future
import client.RequestDispatcher
import client.oauth.OAuthAccessToken
import javax.inject.Inject
import javax.inject.Singleton
import model._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse
import utility.Transformer
import model.KontrollettiToModelParser._
import com.google.inject.ImplementedBy
import utility.FutureUtil._
import play.api.libs.json.Json
import org.joda.time.DateTime
/**
 * @author fbenjamin
 */

case class KioClientException(message: String) extends Exception(message)

trait KioClient {

  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]]


  
}
@Singleton
class KioClientImpl @Inject() (dispatcher: RequestDispatcher, //
                               config: KioClientConfiguration) extends KioClient {
  val logger: Logger = Logger { this.getClass }
  private val transformer = Transformer
  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]] = {
    logger.info("Kio client is calling endpoint" + config.serviceUrl)
    logErrorOnFailure {
      dispatcher.requestHolder(config.serviceUrl) //
        .withHeaders(("Authorization", "Bearer " + accessToken.accessToken)).get()
        .flatMap { response =>
          if (response.status == 200) {
            logger.info("Kio client received http-status-code: " + response.status)
            transformer.parse2Future(response.body).flatMap(transformer.deserialize2Future[List[AppInfo]](_))
          } else {
            val msg = "Request to Kio failed with HTTP-CODE: " + response.status
            logger.error(msg)
            Future.failed(new KioClientException(msg))
          }
        }
    }
  }

}

 