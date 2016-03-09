package client.scm

import scala.concurrent.Future
import play.api.Logger
import play.api.libs.ws.WSResponse
import javax.inject._
import client.RequestDispatcher
import com.google.inject.ImplementedBy
import SCMModel._

@ImplementedBy(classOf[SCMImpl])
sealed trait SCM {

  /**
   * Issues a GET call against the commits-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @param pageNr number of the page to retrieve
   * @return The future with the response of the call
   */
  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNr: Int): Future[WSResponse]

  /**
   * Issues a GET call against the commit-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   */
  def commit(host: String, project: String, repository: String, id: String): Future[WSResponse]

  /**
   * Issues a GET call against the repository-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   */
  def repo(host: String, project: String, repository: String): Future[WSResponse]

  /**
   * Composes an URL of the give repository based on the SCM configured with the matching host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The url that points to the repository.
   */
  def repoUrl(host: String, project: String, repository: String): String
  /**
   * Composes an URL of the give repository based on the SCM configured with the matching host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The url that points to the repository.
   */
  def checkRepoUrl(host: String, project: String, repository: String): String

  /**
   * Composes a diff-URL for the given of the given  repository based on the SCM configured with the matching host.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param source commit-id from where to compare from
   * @param target commit-id from where to compare to
   * @return The url that points diff.
   */
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String

  /**
   * Issues a HEAD operation against the given URL on the SCM.
   * @param host DNS/IP of the SCM server
   * @param url The URL to executed the HEAD operation against.
   * @return The future with the response of the call
   *
   */
  def head(host: String, url: String): Future[WSResponse]

  def resolver(host: String): SCMResolver

  def isGithubServerType(host: String): Boolean

  def toSCMResponse[R <: SCMResource](wsResp: WSResponse): SCMResponse[R] =
    SCMResponse[R](
      httpStatus = wsResp.status,
      headers = wsResp.allHeaders,
      body = wsResp.body
    )
}

@Singleton
class SCMImpl @Inject() (dispatcher: RequestDispatcher, //
                         @Named("github") githubResolver: SCMResolver, //
                         @Named("stash") stashResolver: SCMResolver) extends SCM {
  private val logger: Logger = Logger(this.getClass())

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String], pageNr: Int): Future[WSResponse] = {
    logger.info(s"Commits for $host $project $repository")
    val res: SCMResolver = resolver(host)
    val url = res.commits(host, project, repository)
    get(host, url, since, pageNr)
  }
  def commit(host: String, project: String, repository: String, id: String): Future[WSResponse] = {
    logger.info(s"Commit for $host $project $repository")
    val res: SCMResolver = resolver(host)
    get(host, res.commit(host, project, repository, id), None)
  }
  def repo(host: String, project: String, repository: String): Future[WSResponse] = {
    logger.info(s"Repo for $host $project $repository")
    val res: SCMResolver = resolver(host)
    val url = res.repo(host, project, repository)
    get(host, url, None)
  }

  def repoUrl(host: String, project: String, repository: String): String = {
    logger.info(s"Repo-URL for $host $project $repository")
    val res: SCMResolver = resolver(host)
    res.repoUrl(host, project, repository)
  }
  def checkRepoUrl(host: String, project: String, repository: String): String = {
    logger.info(s"Check-Repo-URL for $host $project $repository")
    val res: SCMResolver = resolver(host)
    res.checkRepoUrl(host, project, repository)
  }
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = {
    logger.info(s"Diff-URL for $host $project $repository")
    val res: SCMResolver = resolver(host)
    res.diffUrl(host, project, repository, source, target)
  }

  def get(host: String, url: String, since: Option[String], pageNr: Int = 1): Future[WSResponse] = {

    logger.info(s"Issuing a GET on $url starting at page $pageNr")
    val res = resolver(host)

    val token = Option(res.accessTokenValue(host))
    if (token == Some("") || !token.isDefined || token.isEmpty) {
      logger.error("No tokens configured for host " + host)
    }

    val req = dispatcher.requestHolder(url)
    res.attachPaginationParams(
      res.attachAuthParams(req, host),
      since,
      pageNr
    ).get()
  }

  def head(host: String, url: String): Future[WSResponse] = {
    logger.info(s"Issuing a HEAD on $url")
    val res = resolver(host)
    val req = res.attachAuthParams(dispatcher.requestHolder(url), host)
    res.checkResource(req)
  }

  def isGithubServerType(host: String): Boolean = resolver(host).isGithubServerType

  def resolver(host: String): SCMResolver = githubResolver.resolve(host) match {
    case Some(resolver) => resolver
    case _ => stashResolver.resolve(host) match {
      case Some(resolver) => resolver
      case _ =>
        val msg = s"Could not resolve SCM context for $host"
        logger.warn(msg)
        throw new IllegalStateException(msg)
    }
  }

}
