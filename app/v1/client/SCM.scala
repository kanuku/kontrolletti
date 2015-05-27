package v1.client

import java.util.ServiceConfigurationError

import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.libs.ws._

sealed trait SCM {
  def committers(host: String, group: String, repo: String): Future[WSResponse]
  def commits(host: String, group: String, repo: String): Future[WSResponse]
}

class SCMImpl extends SCM {

  def committers(host: String, group: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host).get
    val url: String = res.contributors(host, group, repo)
    request(url, res.accessTokenKey, res.accessTokenValue)
  }

  def commits(host: String, project: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host).get
    val url: String = res.commits(host, project, repo)
    request(url, res.accessTokenKey, res.accessTokenValue)
  }

  def request: (String, String, String) => Future[WSResponse] = {
    (url, accessTokenKey, accessTokenValue) =>
      val request = requestHolder(url)
      if (accessTokenValue == null || accessTokenValue.isEmpty()) {
        Logger.info(s"Request with access token " + request.url);
        request.get()
      } else {
        Logger.info(s"Requesting without access token $request.url");
        request.withHeaders(accessTokenKey -> accessTokenValue).get()
      }
  }

  def resolver(host: String) = {
    var result = GithubResolver.resolve(host)
    if (result.isDefined)
      result
    else {
      result = StashResolver.resolve(host)
      if (result.isDefined)
        result
      else
        throw new IllegalStateException(s"Could not resolve SCM context for $host")
    }
  }

  def requestHolder: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }

}

