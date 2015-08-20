package client.kio

import scala.concurrent.Future
import client.JsonParseException
import client.RequestDispatcher
import javax.inject.{ Inject, Singleton }
import model.AppInfo
import client.oauth.OAuthAccessToken

/**
 * @author fbenjamin
 */
trait KioClient {

  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]]

}
@Singleton
class KioClientImpl @Inject() (dispatcher: RequestDispatcher, config: KioClientConfiguration) extends KioClient {

  def apps(accessToken: OAuthAccessToken): Future[List[AppInfo]] = {

    Future.failed(new JsonParseException("...."))
  }

}

