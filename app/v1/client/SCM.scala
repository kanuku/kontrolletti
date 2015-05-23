package v1.client

import scala.concurrent.Future
import play.api.libs.ws._
import play.api.Logger
import play.api.Play.current
import play.api.Logger
import play.api.libs.json.Reads

sealed trait SCM {
  def committers(host: String, group: String, repo: String): Future[WSResponse]
  def commits(host: String, group: String, repo: String): Future[WSResponse]
}

class SCMImpl extends SCM {

  def committers(host: String, group: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host)
    val url: String = res.contributors(group, repo)
    request(url, res.accessTokenKey, res.accessTokenValue)
  }
  def commits(host: String, project: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host)
    val url: String = res.commits(project, repo)

    request(url, res.accessTokenKey, res.accessTokenValue)
  }

  def request: (String, String, String) => Future[WSResponse] = {
    (url, accessTokenKey, accessTokenValue) =>
    val request = requestHolder(url)
      if (accessTokenValue != null || accessTokenValue.isEmpty()) {
        Logger.info(s"Request with access token "+request.url  );
        request.get()
      } else {
        Logger.info(s"Requesting without access token $request.url");
        request.withHeaders(accessTokenKey -> accessTokenValue).get()
      }
  }

  def resolver = GithubResolver.resolve orElse StashResolver.resolve

  def requestHolder: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }

}

/**
 * Resolves the communication context for the SCM.  Holds configurations like URL's
 * and Headers for communicating with the Rest Interface of a SCM server.
 * The idea of this trait is to minimize the gap between the communication with different SCMs.
 */
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
  val accessTokenValue = "????????????????????"
}

//https://stash.zalando.net/rest/api/1.0/projects/doc/repos/ci-cd/commits
private object StashResolver extends SCMResolver {
  def names: List[String] = List("stash.zalando.net")
  def api_host: String = s"https://stash.zalando.net"
  def contributors(project: String, repo: String) = s"$api_host/rest/api/1.0/projects/$project/repos/$repo/contributors"
  def commits(project: String, repo: String) = s"$api_host/rest/api/1.0/projects/$project/repos/$repo/commits"

  // Authorization variables
  def accessTokenKey = "X-Auth-Token"
  val accessTokenValue = "???????????????????"
}

