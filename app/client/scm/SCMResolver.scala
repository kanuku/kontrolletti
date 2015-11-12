package client.scm

import scala.collection.JavaConverters.{ asScalaBufferConverter, _ }
import play.api.Logger
import play.api.Play.current
import javax.inject._
import client.RequestDispatcher
import configuration.SCMConfiguration

/**
 * Resolves the URL's in the communication context with the SCM REST API. <br> Holds configurations like URL's
 * and Headers for communicating with the Rest Interface of a SCM server.
 * The idea of this trait is to minimize the gap between the communication with different SCMs.
 */
sealed trait SCMResolver {
  val logger: Logger = Logger(this.getClass())
  def hostType: String

  lazy val antecedents: Map[String, String] = {
    val result = combineMap(config().urlPrecedent(hostType))
    logger.info(s"Loaded URL-antecedentes for $hostType:" + result.size)
    result
  }
  lazy val succeeders: Map[String, String] = {
    val result = combineMap(config().urlSucceeder(hostType))
    logger.info(s"Loaded URL-succeeders for $hostType:" + result.size)
    result
  }
  lazy val authTokens: Map[String, String] = {
    val result = combineMap(config().authToken(hostType))
    logger.info(s"Loaded authToken for $hostType:" + result.size)
    result
  }
  lazy val authUsers: Map[String, String] = {
    val result = combineMap(config().authUser(hostType))
    logger.info(s"Loaded authUser for $hostType:" + result.size)
    result
  }

  private def combineMap(input: Map[Int, String]) = for {
    (host, key) <- hosts
    if input.contains(key)
    value <- input.get(key)
  } yield host -> value

  def config(): SCMConfiguration

  /**
   * The list hosts that can handle the calls implemented by this client.
   *  @return the list of `hosts` this client can communicate with.
   */
  lazy val hosts: Map[String, Int] = config.hosts(hostType)

  /**
   * Checks if this client is configured to communicate with a given host.
   * @param  host The host to be tested(something like github.com or stash.zalan.do)
   * @return true if this client is configured to communicate with the host.
   */
  //TODO This can be removed when github implementation returns an empty parameter for userAuth.
  def isCompatible(host: String): Boolean = hosts.contains(host)

  /**
   *  Url for listing commits of a repository in the given `project` at the given `host`.
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @param repo repository
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
   * Resolves to itself if the host matches to any of the configured `hosts`
   * Otherwise to an instance of None
   * @param host host of the SCM server
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
   * @param host host of the SCM server
   * @return either an error(left) or the normalized URI (right)
   */
  def repoUrl(host: String, project: String, repository: String): String

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param host host of the SCM server
   * @param project project where the repository belongs to
   * @param repo repository
   * @return either an error(left) or the normalized URI (right)
   */
  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String

  /**
   * The access-token key to use for accessing the SCM Rest api.
   */
  def accessTokenHeader(host: String): (String, String)

  /**
   * Builds a QueryParameter to get maximal number of Items in the response.
   *  @return query parameter (Tupple)
   */
  def maximumPerPageQueryParameter(): (String, String)

  /**
   * Builds a Query parameter to retrieve commits after.
   *  @param since since parameter
   *  @return query parameter (Tupple)
   */
  def sinceCommitQueryParameter(since: String): (String, String)

  /**
   * Returns the authorization/authentication user Parameter for the given host
   * @param host
   *  @return Username for the authorization/authentication
   */
  def authUser(host: String): String = authUsers.getOrElse(host, "")

  /**
   * Builds a header authorization query parameter for the user.
   *  @return query parameter (Tupple)
   */
  def authUserHeaderParameter(host: String): (String, String)

  /**
   * Builds a Query parameter that indicates which page number of the result Set should be returned.
   *  @param pageNr page number
   */
  def startAtPageNumber(pageNr: Int): (String, String)

  def isGithubServerType(): Boolean
  /**
   * The access-token value to use for accessing the SCM Rest api.
   */
  def accessTokenValue(host: String): String = authTokens.getOrElse(host, "")

}

@Singleton
class GithubResolver @Inject() (config: SCMConfiguration) extends SCMResolver {
  def hostType = "github"

  def config() = config

  def commits(host: String, project: String, repository: String) = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/repos/$project/$repository/commits"
  }
  def commit(host: String, project: String, repository: String, id: String): String = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/repos/$project/$repository/commits/$id"
  }

  def repo(host: String, project: String, repository: String) = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/repos/$project/$repository"
  }
  def repoUrl(host: String, project: String, repository: String) = s"https://$host/$project/$repository"

  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = {
    val antecedent = antecedents(host)
    s"$antecedent$host/$project/$repository/compare/$source...$target"
  }

  def accessTokenHeader(host: String) = ("access_token" -> accessTokenValue(host))
  def authUserHeaderParameter(host: String): (String, String) = ("" -> "")
  // SCM Specific mappings
  def maximumPerPageQueryParameter = ("per_page" -> "100")
  def isGithubServerType(): Boolean = true
  def sinceCommitQueryParameter(since: String) = ("date" -> since)
  def startAtPageNumber(pageNr: Int) = ("page" -> pageNr.toString())
}

@Singleton
class StashResolver @Inject() (config: SCMConfiguration) extends SCMResolver {

  def hostType = "stash"
  def config() = config

  def commits(host: String, project: String, repository: String) = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/projects/$project/repos/$repository/commits"
  }
  def commit(host: String, project: String, repository: String, id: String): String = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/projects/$project/repos/$repository/commits/$id"
  }

  def repo(host: String, project: String, repository: String) = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"$antecedent$host$succeeder/projects/$project/repos/$repository"
  }
  def repoUrl(host: String, project: String, repository: String) = s"https://$host/projects/$project/repos/$repository/browse"

  def diffUrl(host: String, project: String, repository: String, source: String, target: String): String = {
    val antecedent = antecedents(host)
    val succeeder = succeeders(host)
    s"https://$host$succeeder/projects/$project/repos/$repository/compare/commits?from=$source&to=$target"
  }
  // Authorization variables
  def accessTokenHeader(host: String) = ("X-Auth-Token" -> accessTokenValue(host))
  def authUserHeaderParameter(host: String): (String, String) = ("X-Auth-User" -> authUser(host))

  def maximumPerPageQueryParameter() = ("limit" -> "10000")
  def isGithubServerType: Boolean = false
  def sinceCommitQueryParameter(since: String) = ("since" -> since)
  def startAtPageNumber(pageNr: Int) = ("start" -> (pageNr - 1).toString())
}
