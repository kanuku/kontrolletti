package v1.client
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger

object GithubEndpoints {
  def name: String = ("github.com")
  def api_host: String = ("https://api.github.com")
  def organization: String = (api_host + "/orgs/")
  def contributors(owner: String, repo: String) = s"$api_host/repos/$owner/$repo/contributors"
  val accessToken = "5cef74508c111c6fe3b35ca9a26fe9a8df4bc456"
}

class Github extends SCM {

  import GithubEndpoints._

  def name = "github.com"

  def committersFrom(group: String, repo: String): Future[WSResponse] = {
    val url: String = contributors(group, repo)
    Logger.debug(s"Requesting users with $name from $url");
    WS    .
    url(url)
    .withHeaders("access_token" -> accessToken).get()
  }

}