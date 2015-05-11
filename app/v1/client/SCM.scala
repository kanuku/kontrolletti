package v1.client

import scala.concurrent.Future

import play.api.libs.ws.WSResponse
 

trait SCM {
  def name: String
  def committers(group: String, repo: String): Future[WSResponse]
  def user(user: String): Future[WSResponse]
}


 