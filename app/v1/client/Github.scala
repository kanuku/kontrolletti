package v1.client
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger

object Githubs {
  def accessTokenKey = "access_token"
  def name: String = ("github.com")
  def api_host: String = ("https://api.github.com")
  def organization: String = (api_host + "/orgs/")
  def contributors(owner: String, repo: String) = s"$api_host/repos/$owner/$repo/contributors"
  def users(user: String) = s"$api_host/users/$user"
  val accessTokenValue = "897674c1118fa83e8819dbab7fa501ddec3dfb24"
}

class Github extends SCM {

  import Githubs._

  def name = "github.com"

  def committers(group: String, repo: String): Future[WSResponse] = {
    val url: String = contributors(group, repo)
    Logger.debug(s"Requesting contributors from $url");
    request(url)
  }

  def user(user: String): Future[WSResponse] = {
    val url: String = users(user)
    Logger.debug(s"Requesting user with $name from $url");
    request(url)
  }
  def request: (String) => Future[WSResponse] = {
    (url) =>
      requestHolder(url).
        withHeaders(accessTokenKey -> accessTokenValue).get()
  }

  def requestHolder: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }

}