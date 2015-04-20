package v1.client
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.Play.current
import scala.util.{ Success, Failure }

trait GithubEndpoints {

  def host: String = ("github.com")
  def api_host: String = ("api.github.com")
  def organization: String = ("https://api.github.com/orgs/")

}



object GithubClientUsers extends GithubEndpoints { 
  val accessToken = "4982d8758ca914e9cbf1d57c32dc61bbc7c5a985"
  def members(org: String): Future[WSResponse] = {
    val url: String = organization+org + "/members?access_token=" + accessToken
    val holder: WSRequestHolder = WS.url(url)
    holder.withHeaders("access_token" -> "4982d8758ca914e9cbf1d57c32dc61bbc7c5a985").get()
  }
}



