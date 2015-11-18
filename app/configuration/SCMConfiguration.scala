package configuration

import scala.collection.mutable.MultiMap
import play.api.{ Logger, Play }
import scala.collection.immutable.HashMap
import scala.collection.JavaConverters.{ asScalaBufferConverter, _ }
/**
 * Configuration loader for parameters necessary in the SCM client.
 */
sealed trait SCMConfiguration {
  /**
   * Loads the list of configured scm hostnames for the given host type.
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing a `hosts` and a unique number identifying the host.
   */
  def hosts(hostType: String): Map[String, Int]

  /**
   * Loads the list of projects allowed for the given hostType.
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing the unique identifying number(host) and a set of allowed projects for that host.
   */
  def allowedProjects(hostType: String): Map[Int, Set[String]]

  /**
   * Loads the list of Rest Api precedents for the given host-type.
   * I.e.: For using the SCM client to talk to Github and-or Github-Enterprise, different
   * are necessary: <br>
   *  Github uses [https://api.] as precedent<br>
   *  Github-Enterprise uses [https://] as precedent<p>
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing the unique identifying number(host) and the URL precedent for that host.
   */
  def urlPrecedent(hostType: String): Map[Int, String]

  /**
   * Loads the list of Rest Api antecedents for the given host-type.
   * Normally this is only necessary for github-enterprise and stash.
   * I.e.: For using the SCM client to talk to Github-Enterprise and-or stash. <br>
   *  Stash uses https://stash.com[/rest/api/1.0/] as precedent<br>
   *  Github-Enterprise uses https://github-enterprise.com[/api/v3] as precedent<p>
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing the unique identifying number(host) and the URL succeeder for that host.
   */
  def urlSucceeder(hostType: String): Map[Int, String]

  /**
   * Loads the list of authentication-tokens of the Rest API for the given host-type.
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing the unique identifying number(host) the authorization token for that host.
   */
  def authToken(hostType: String): Map[Int, String]

  /**
   * Loads the list of authentication-users of the REST API for the given host.
   * @param hostType - Type of SCM(github/stash).
   * @return a map containing the unique identifying number(host) the authorization user for that host.
   */
  def authUser(hostType: String): Map[Int, String]

}

class SCMConfigurationImpl extends SCMConfiguration with ConfigurationDefaults {

  private val logger: Logger = Logger(this.getClass())
  private val startHost = 0
  private val maxHosts = 10

  def hosts(hostType: String): Map[String, Int] = {
    logger.info(s"Loading all hosts for $hostType")
    Map((for {
      number <- startHost to maxHosts
      host <- Play.current.configuration.getString(s"client.scm.$hostType.host.$number")
    } yield host -> number): _*)
  }

  def urlSucceeder(hostType: String): Map[Int, String] = {
    logger.info(s"Loading all url-succeeders for $hostType")
    readValues(hostType, s"client.scm.$hostType.urlSucceeder")

  }
  def urlPrecedent(hostType: String): Map[Int, String] = {
    logger.info(s"Loading all url-precedents for $hostType")
    readValues(hostType, s"client.scm.$hostType.urlPrecedent")
  }

  private def readValues(hostType: String, key: String): Map[Int, String] = {
    Map((for {
      number <- startHost to maxHosts
      host <- Play.current.configuration.getString(s"$key.$number")
    } yield number -> host): _*)
  }

  def authToken(hostType: String): Map[Int, String] = {
    logger.info(s"Loading all auth-tokens for $hostType")
    readValues(hostType, s"client.scm.$hostType.authToken")

  }
  def authUser(hostType: String): Map[Int, String] = {
    logger.info(s"Loading all auth-users for $hostType")
    readValues(hostType, s"client.scm.$hostType.user")
  }

  def allowedProjects(hostType: String): Map[Int, Set[String]] = {
    logger.info(s"Loading allowed projects for $hostType")
    val key = s"client.scm.$hostType.allowedProjects"
    Map((for {
      number <- startHost to maxHosts
      host <- Play.current.configuration.getStringList(s"$key.$number")
    } yield number -> host.asScala.toSet): _*)
  }

}