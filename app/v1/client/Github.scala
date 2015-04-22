package v1.client
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger 
trait GithubEndpoints {
  def name: String = ("github.com")
  def api_host: String = ("https://api.github.com")
  def organization: String = (api_host + "/orgs/")
  def contributors(owner: String, repo: String) = s"$api_host/repos/$owner/$repo/contributors"

}

class Github extends SCM with GithubEndpoints {

  val accessToken = "0995b62373fe2e8ce64487d0b19bbeec6a8535ad"

  def contributorsByRepo(group: String, repo: String): Future[WSResponse] = {
    val url: String = contributors(group, repo)
    Logger.debug(s"Requesting users with $name from $url");
    WS.url(url).withHeaders("access_token" -> accessToken).get()
  }
}