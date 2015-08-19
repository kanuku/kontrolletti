package client.kio

import scala.concurrent.Future

import client.JsonParseException
import client.RequestDispatcher
import javax.inject.{Inject, Singleton}
import model.AppInfo

/**
 * @author fbenjamin
 */
trait KioClient {

  def appIds(): Future[List[String]]

  def apps(appIds: List[String]): Future[List[AppInfo]]

}
@Singleton
class KioClientImpl @Inject() (dispatcher: RequestDispatcher) extends KioClient {

  def appIds(): Future[List[String]] = {

    Future.failed(new JsonParseException("...."))
  }

  def apps(appIds: List[String]): Future[List[AppInfo]] = {

    Future.failed(new JsonParseException("...."))
  }

}

