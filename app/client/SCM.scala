package client

import scala.concurrent.Future

import play.api.Logger
import play.api.libs.ws.WSResponse
import javax.inject._
sealed trait SCM {

  /**
   * Issues a GET call against the commits-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   */
  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[WSResponse]
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
   * Issues a GET call against the ticket/issue-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   */
  def tickets(host: String, project: String, repository: String): Future[WSResponse]

  /**
   * Composes an URL of the give repository based on the SCM configured with the matching host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The url that points to the repository.
   */
  def repoUrl(host: String, project: String, repository: String): String

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
   * Issues a HEAD operation against the give url on the SCM.
   * @param host DNS/IP of the SCM server
   * @param url The url to executed the HEAD operation against.
   * @return The future with the response of the call
   *
   */
  def head(url: String): Future[WSResponse]

  def resolver(host: String): Option[SCMResolver]

}
@Singleton
class SCMImpl @Inject() (dispatcher: RequestDispatcher) extends SCM {
  private val logger: Logger = Logger(this.getClass())

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[WSResponse] = ???
  def commit(host: String, project: String, repository: String, id: String): Future[WSResponse] = ???
  def repo(host: String, project: String, repository: String): Future[WSResponse] = ???
  def committers(host: String, project: String, repository: String): Future[WSResponse] = ???
  def tickets(host: String, project: String, repository: String): Future[WSResponse] = ???
  def repoUrl(host: String, project: String, repository: String): String = {
    val res: SCMResolver = resolver(host).get
    res.repoUrl(host, project, repository)
  }
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = {
    val res: SCMResolver = resolver(host).get
    res.diffUrl(host, project, repository, source, target)
  }

  def head(url: String): Future[WSResponse] = ???

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
}

