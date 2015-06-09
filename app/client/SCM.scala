package client

import java.util.ServiceConfigurationError

import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.libs.ws._

sealed trait SCM {

  def committers(host: String, project: String, repo: String): Future[WSResponse]

  def commits(host: String, project: String, repo: String): Future[WSResponse]

  def normalize(host: String, project: String, repo: String): String

  /**
   * Checks if a repository exists with a HTTP Head request to the repository url.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   *
   */
  def repoExists(host: String, project: String, repo: String): Future[WSResponse]

}

class SCMImpl extends SCM {
  private val logger: Logger = Logger(this.getClass())
  
  type Call = (WSRequestHolder) => Future[WSResponse]
  
  val GET: Call = { requestHolder => requestHolder.get() }
  val HEAD: Call = { requestHolder => requestHolder.head() }

  def committers(host: String, project: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host).get
    val url: String = res.contributors(host, project, repo)
    request(GET, url, res.accessTokenKey, res.accessTokenValue)
  }

  def commits(host: String, project: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host).get
    val url: String = res.commits(host, project, repo)
    request(GET, url, res.accessTokenKey, res.accessTokenValue)
  }
  def repoExists(host: String, project: String, repo: String): Future[WSResponse] = {
    val res: SCMResolver = resolver(host).get
    val url: String = res.repo(host, project, repo)
    request(HEAD, url, res.accessTokenKey, res.accessTokenValue)
  }

  def normalize(host: String, project: String, repo: String): String = {
    val res: SCMResolver = resolver(host).get
    res.normalize(host, project, repo)
  }

  def request(call: Call, url: String, accessTokenKey: String, accessTokenValue: String): Future[WSResponse] = {
    val request = requestHolder(url)
    if (accessTokenValue == null || accessTokenValue.isEmpty()) {
      logger.info(s"$url without access-token ");
      call(request)
    } else {
      logger.info(s"$url with access-token");
      call(request.withHeaders(accessTokenKey -> accessTokenValue))
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

