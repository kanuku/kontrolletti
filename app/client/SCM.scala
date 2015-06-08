package client

import java.util.ServiceConfigurationError

import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.libs.ws._

sealed trait SCM {

  def committers(host: String, group: String, repo: String): Future[WSResponse]

  def commits(host: String, group: String, repo: String): Future[WSResponse]

  def normalize(host: String, project: String, repo: String): String

  /**
   * Checks if a repository exists with a HTTP Head request to the repository url.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return on the right true if the response was 200 or false if it was 404/301 or a Error message left for any other HTTP code.
   *
   */
  def doesRepoExist(host: String, project: String, repo: String): Either[String, Boolean]

}

class SCMImpl extends SCM {
  private val logger: Logger = Logger(this.getClass())

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

  def normalize(host: String, project: String, repo: String): String = {
    val res: SCMResolver = resolver(host).get
    res.normalize(host, project, repo)
  }

  def request: (String, String, String) => Future[WSResponse] = {
    (url, accessTokenKey, accessTokenValue) =>
      val request = requestHolder(url)
      if (accessTokenValue == null || accessTokenValue.isEmpty()) {
        logger.info(s"$url with access-token ");
        request.get()
      } else {
        logger.info(s"$url without access-token");
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

  def doesRepoExist(host: String, project: String, repo: String): Either[String, Boolean] = {

    Left("")
  }

  def requestHolder: (String) => WSRequestHolder = {
    (url) => WS.url(url)
  }

}

