package client

import java.util.ServiceConfigurationError

import scala.collection.JavaConverters.asScalaBufferConverter

import play.api.Logger

/**
 * Resolves the communication context for the SCM REST API. <br> Holds configurations like URL's
 * and Headers for communicating with the Rest Interface of a SCM server.
 * The idea of this trait is to minimize the gap between the communication with different SCMs.
 */
sealed trait SCMResolver {
  private val logger: Logger = Logger(this.getClass())
  def name: String

  /**
   * Hosts configuration property.
   */
  def hostsProperty: String = s"client.$name.hosts"

  /**
   * The list hosts that can handle the calls implemented by this client.
   *  @return the list of `hosts` this client can communicate with.
   */
  lazy val hosts: Set[String] = {
    import collection.JavaConverters._
    val result = play.Play.application.configuration.getStringList(hostsProperty).asScala.toSet
    logger.info(s"Configuring $name with hosts $result")
    result
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
   * Url for listing contributors of a repository in the given `project` at the given `host`.
   * @param repo repository
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @return The url
   */
  def contributors(host: String, project: String, repo: String): String

  /**
   *  Url for listing commits of a repository in the given `project` at the given `host`.
   * @param repo repository
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @return The url
   */
  def commits(host: String, project: String, repo: String): String

  /**
   *  Url for the repository in the given user namespace at the given `host`.
   * @param repo repository
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @return The url
   */
  def repo(host: String, project: String, repo: String): String
  
  /**
   * Resolves to itself if the host matches to any of the configured `hosts`
   * Otherwise to an instance of None
   */
  def resolve(host: String): Option[SCMResolver] = host match {
    case host if hosts.contains(host) =>
      Some(this)
    case _ =>
      None
  }

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param url url of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def repoUrl(host: String, project: String, repo: String): String
  
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
  def accessTokenKey: String

  /**
   * The access-token value to use for accessing the SCM Rest api.
   */
  lazy val accessTokenValue = {
    val input = play.Play.application.configuration.getString(accessTokenProperty)
    if (input == null || input.isEmpty())
      logger.error(s"Configuration($accessTokenProperty) for the client is missing")
    else
      logger.info(s"Loaded Token configuration for $accessTokenProperty")
    input
  }

}

object GithubResolver extends SCMResolver {
  def name = "github"
  private val antecedent = "https://api."

  def contributors(host: String, project: String, repo: String) = s"$antecedent$host/repos/$project/$repo/contributors"
  def commits(host: String, project: String, repo: String) = s"$antecedent$host/repos/$project/$repo/commits"
  def repo(host: String, project: String, repo: String) = s"$antecedent$host/repos/$project/$repo"
  def repoUrl(host: String, project: String, repo: String) = s"https://$host/$project/$repo"
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = ""
  // Authorization variables
  def accessTokenKey = "access_token"
}

object StashResolver extends SCMResolver {
  def name = "stash"
  private val antecedent = "https://"

  def contributors(host: String, project: String, repo: String) = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repo/contributors"
  def commits(host: String, project: String, repo: String) = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repo/commits"
  def repo(host: String, project: String, repo: String) = s"$antecedent$host/rest/api/1.0/projects/$project/repos/$repo"
  def repoUrl(host: String, project: String, repo: String) = s"https://$host/projects/$project/repos/$repo/browse"
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = ""
  // Authorization variables
  def accessTokenKey = "X-Auth-Token"

}

