package v1.client
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger

object Githubs {
  def accessTokenKey="access_token"
  def name: String = ("github.com")
  def api_host: String = ("https://api.github.com")
  def organization: String = (api_host + "/orgs/")
  def contributors(owner: String, repo: String) = s"$api_host/repos/$owner/$repo/contributors"
  val accessTokenValue = "4231409123784109287wd239847aefsg"
}

class Github extends SCM {

  import Githubs._

  def name = "github.com"

  def committersFrom(group: String, repo: String): Future[WSResponse] = {
    val url: String = contributors(group, repo)
    Logger.debug(s"Requesting users with $name from $url");
    request(url).withHeaders( accessTokenKey-> accessTokenValue).get()
  }

  def request: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }
}