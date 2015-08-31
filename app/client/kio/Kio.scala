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
import model.KontrollettiToModelParser._

/**
 * @author fbenjamin
 */
trait Kio {

  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]]

}


@Singleton
class KioImpl @Inject() (dispatcher: RequestDispatcher, config: KioClientConfiguration) extends Kio {
  private val logger: Logger = Logger { this.getClass }
  private val transformer = Transformer
  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]] = {
    dispatcher.requestHolder(config.serviceUrl) //
      .withHeaders(("Authorization", "Bearer " + accessToken.accessToken)).get()
      .flatMap { response =>
        logger.info("Received http-status-code: " + response.status)
        transformer.transform[List[AppInfo]](response.body)
      }
  }

}

