package v1.client

import scala.concurrent.Future
import play.api.libs.ws._
import play.api.Logger
import play.api.Play.current
import play.api.Logger
import play.api.libs.json.Reads

sealed trait Callable{
  def f:Future[WSResponse]
}

sealed trait SCMClient {
  def committers(host: String, group: String, repo: String): Future[WSResponse]
  def commits(host: String, group: String, repo: String): Future[WSResponse]
}

class SCMClientImpl extends SCMClient {

  def committers(host: String, group: String, repo: String): Future[WSResponse] = {
    val res = resolver(host)
    val url: String = res.contributors(group, repo)
    Logger.debug(s"Requesting contributors from $url");
    request(url, res.accessTokenKey, res.accessTokenValue)
  }
  def commits(host: String, project: String, repo: String): Future[WSResponse] = {
    val res = resolver(host)
    val url: String = res.commits(project, repo)
    Logger.debug(s"Requesting commits from $url");
    request(url, res.accessTokenKey, res.accessTokenValue)
  }

  def request: (String, String, String) => Future[WSResponse] = {
    (url, accessTokenKey, accessTokenValue) => requestHolder(url).withHeaders(accessTokenKey -> accessTokenValue).get()
  }

  def resolver = GithubResolver.resolve orElse StashResolver.resolve

  def requestHolder: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }

}

sealed trait SCMResolver {

  /**
   *   The list hosts that can handle the calls implemented by this resolver.
   *  @return the list of `host`.
   */
  def names: List[String]
  def contributors(project: String, repo: String): String
  def commits(project: String, repo: String): String
  //  def users(user: String): String

  // Authorization parameters
  def accessTokenValue: String
  def accessTokenKey: String

  def resolve: PartialFunction[String, SCMResolver] = {
    case host if names.contains(host) => this
  }

}

object GithubResolver extends SCMResolver {
  def names: List[String] = List("github.com")
  def api_host: String = "https://api.github.com"
  def organization: String = api_host + "/orgs/"
  def contributors(project: String, repo: String) = s"$api_host/repos/$project/$repo/contributors"
  def commits(project: String, repo: String) = s"$api_host/repos/$project/$repo/commits"

  // Authorization variables
  def accessTokenKey = "access_token"
  val accessTokenValue = "897674c1118fa83e8819dbab7fa501ddec3dfb24"
}

//https://stash.zalando.net/rest/api/1.0/projects/doc/repos/ci-cd/commits
object StashResolver extends SCMResolver {
  def names: List[String] = List("stash.zalando.net")
  def api_host: String = s"https://stash.zalando.net"
  def contributors(project: String, repo: String) = s"$api_host//rest/api/1.0/projects/$project/repos/$repo/contributors"
  def commits(project: String, repo: String) = s"$api_host//rest/api/1.0/projects/$project/repos/$repo/commits"

  // Authorization variables
  def accessTokenKey = "access_token"
  val accessTokenValue = "897674c1118fa83e8819dbab7fa501ddec3dfb24"
}

