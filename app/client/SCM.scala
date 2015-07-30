package client

import scala.concurrent.Future

import play.api.Logger
import play.api.libs.ws.WSResponse

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
  def repos(host: String, project: String, repository: String): Future[WSResponse]

  /**
   * Issues a HEAD call against the repository-URL on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   *
   */
  def isRepo(host: String, project: String, repository: String): Future[WSResponse]

  /**
   * Issues a HEAD call against the diff-URL on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   *
   */
  def isDiff(host: String, project: String, repository: String): Future[WSResponse]

  /**
   * Issues a GET call against the ticket/issue-endpoint on the SCM.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The future with the response of the call
   */
  def tickets(host: String, project: String, repository: String, since: Option[String], untill: Option[String]): Future[WSResponse]

  /**
   * Composes an URL of the give repository based on the SCM configured with the matching host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return The url that points to the repository.
   */

  def url(host: String, project: String, repository: String): String

}

class SCMImpl extends SCM {
  private val logger: Logger = Logger(this.getClass())

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[WSResponse] = ???
  def commit(host: String, project: String, repository: String, id: String): Future[WSResponse] = ???
  def repos(host: String, project: String, repository: String): Future[WSResponse] = ???
  def committers(host: String, project: String, repository: String): Future[WSResponse] = ???
  def isRepo(host: String, project: String, repository: String): Future[WSResponse] = ???
  def isDiff(host: String, project: String, repository: String): Future[WSResponse] = ???
  def tickets(host: String, project: String, repository: String, since: Option[String], untill: Option[String]): Future[WSResponse] = ???
  def url(host: String, project: String, repository: String): String = ???

}

