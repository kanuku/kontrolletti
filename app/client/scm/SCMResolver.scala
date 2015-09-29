package client.scm

import java.util.ServiceConfigurationError

import scala.collection.JavaConverters.asScalaBufferConverter
import play.api.Play.current
import play.api.Logger
import collection.JavaConverters._

/**
 * Resolves the URL's in the communication context with the SCM REST API. <br> Holds configurations like URL's
 * and Headers for communicating with the Rest Interface of a SCM server.
 * The idea of this trait is to minimize the gap between the communication with different SCMs.
 */
sealed trait SCMResolver {
  private val logger: Logger = Logger(this.getClass())
  def name: String

  /**
   * Hosts configuration property.
   */
  def hostsProperty: String = s"client.$name.host"

  /**
   * The list hosts that can handle the calls implemented by this client.
   *  @return the list of `hosts` this client can communicate with.
   */
  lazy val hosts: Set[String] = {
    current.configuration.getString(hostsProperty) match {
      case Some(result) =>
        logger.info(s"Configuring $name with hosts $result")
        Set(result)
      case None =>
        logger.warn(s"Failed to load configuration $name and property $hostsProperty")
        Set()
    }
  }

  /**
   * Checks if this client is configured to communicate with a given host.
   * @param  host The host to be tested(something like github.com or stash.zalan.do)
   * @return true if this client is configured to communicate with the host.
   */
  def isCompatible(host: String): Boolean = {
    !hosts.find { x => x == host }.isEmpty
  }

  /**
   *  Url for listing commits of a repository in the given `project` at the given `host`.
   * @param repo repository
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @return The url
   */
  def commits(host: String, project: String, repository: String): String

  /**
   *  Url for fetching the commit information from a repository in the given `project` at the given `host`.
   * @param repo repository
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @param id commit-id to be returned
   * @return The url
   */
  def commit(host: String, project: String, repository: String, id: String): String

  /**
   *  Url for the `repository` in the given user `project` at the given `host`.
   * @param repository repository.
   * @param host host of the SCM server.
   * @param project project where the repository belongs to.
   * @return The url.
   */
  def repo(host: String, project: String, repository: String): String

  /**
   *  Url for the tickets at the `repository` in the given `project` at the given `host`.
   * @param repository repository.
   * @param host host of the SCM server.
   * @param project project where the repository belongs to.
   * @return The url.
   */
  def tickets(host: String, project: String, repository: String): String

  /**
   * Resolves to itself if the host matches to any of the configured `hosts`
   * Otherwise to an instance of None
   */
  def resolve(host: String): Option[SCMResolver] = {
    host match {
      case host if hosts.contains(host) =>
        Option(this)
      case _ =>
        None
    }
  }

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param url url of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def repoUrl(host: String, project: String, repository: String): String

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param url url of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String

  /**
   * The access-token property for the access-token the rest api of this client.
   */
  def accessTokenProperty: String = s"client.$name.accessToken"

  /**
   * The access-token key to use for accessing the SCM Rest api.
   */
  def accessTokenQueryParameter: (String, String)

  /**
   * Builds a QueryParameter to get maximal number of Items in the response.
   *  @returns query parameter (Tupple)
   */
  def maximumPerPageQueryPararmeter: (String, String)

  /**
   * Builds a Query parameter to retrieve commits after.
   *  @param since since parameter
   *  @returns query parameter (Tupple)
   */
  def sinceCommitQueryParameter(since: String): (String, String)

  /**
   * Builds a Query parameter that indicates which page number of the result Set should be returned.
   *  @param pageNr page number
   */
  def startAtPageNumber(pageNr: Int): (String, String)

  def isGithubServerType: Boolean
  /**
   * The access-token value to use for accessing the SCM Rest api.
   */
  lazy val accessTokenValue: String = Option(play.Play.application.configuration.getString(accessTokenProperty)) match {
    case None =>
      logger.error(s"Configuration($accessTokenProperty) for the client is missing")
      "Error"
    case Some(token) if token.isEmpty =>
      logger.warn(s"$accessTokenProperty property is empty, configure a token -> $accessTokenProperty=token")
      token
    case Some(token) =>
      logger.info(s"Loaded Token configuration from $accessTokenProperty")
      token
  }
}

object GithubResolver extends SCMResolver {
  def name = "github"
  private val apiAntecedent = "https://api."
  private val antecedent = "https://"
  def commits(host: String, project: String, repository: String) = s"$apiAntecedent$host/repos/$project/$repository/commits"
  def commit(host: String, project: String, repository: String, id: String): String = s"$apiAntecedent$host/repos/$project/$repository/commits/$id"
  def tickets(host: String, project: String, repository: String): String = s"$apiAntecedent$host/repos/$project/$repository/commits"

  def repo(host: String, project: String, repository: String) = s"$apiAntecedent$host/repos/$project/$repository"
  def repoUrl(host: String, project: String, repository: String) = s"$antecedent$host/$project/$repository"
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = s"$antecedent$host/$project/$repository/compare/$source...$target"

  def accessTokenQueryParameter = ("access_token" -> accessTokenValue)
  // SCM Specific mappings
  def maximumPerPageQueryPararmeter = ("per_page" -> "100")
  def isGithubServerType: Boolean = true
  def sinceCommitQueryParameter(since: String) = ("date" -> since)
  def startAtPageNumber(pageNr: Int) = ("page" -> pageNr.toString())
}

object StashResolver extends SCMResolver {
  def name = "stash"
  private val antecedent = "https://"
  def commits(host: String, project: String, repository: String) = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repository/commits"
  def commit(host: String, project: String, repository: String, id: String): String = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repository/commits/$id"
  def tickets(host: String, project: String, repository: String): String = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repository/commits"

  def repo(host: String, project: String, repository: String) = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repository"
  def repoUrl(host: String, project: String, repository: String) = repo(host, project, repository)
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repository/compare/commits?from=$source&to=$target"

  // Authorization variables
  def accessTokenQueryParameter = ("X-Auth-Token" -> accessTokenValue)
  def maximumPerPageQueryPararmeter = ("limit" -> "10000")
  def isGithubServerType: Boolean = false
  def sinceCommitQueryParameter(since: String) = ("since" -> since)
  def startAtPageNumber(pageNr: Int) = ("start" -> (pageNr - 1).toString())

}
