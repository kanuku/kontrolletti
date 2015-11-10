package configuration

import scala.collection.mutable.MultiMap
import play.api.{ Logger, Play }
import scala.collection.immutable.HashMap

/**
 * Configuration loader for parameters necessary in the SCM client.
 */
sealed trait SCMConfiguration {
  /**
   * Loads the list of configured scm hostnames for the given host type.
   * @param hostType - Type of SCM(github/stash).
   * @return the list of `hosts` this client can communicate with.
   */
  def hosts(hostType: String): Map[String, Int]

  /**
   * Loads the list of Rest Api precedents for the given host-type.
   * I.e.: For using the SCM client to talk to Github and-or Github-Enterprise, different
   * are necessary: <br>
   *  Github uses [https://api.] as precedent<br>
   *  Github-Enterprise uses [https://] as precedent<p>
    * @param hostType - Type of SCM(github/stash).
   *  @return The precedent for the Rest API URLs.
   */
  def urlPrecedent(hostType: String): Map[Int, String]

  /**
   * Loads the list of authentication-tokens of the Rest API for the given host-type.
   * @param hostType - Type of SCM(github/stash).
   * @return The authentication token that belongs to the given host.
   */
  def authToken(hostType: String): Map[Int, String]

  /**
   * Loads the list of authentication-users of the REST API for the given host.
   * @param hostType - Type of SCM(github/stash).
   * @return The authentication user that belongs to the given host.
   */
  def authUser(hostType: String): Map[Int, String]

}

class SCMConfigurationImpl extends SCMConfiguration with ConfigurationDefaults {

  private val logger: Logger = Logger(this.getClass())
  private val maxHosts = 10

  def hosts(hostType: String): Map[String, Int] = {
    logger.info(s"Loading all hosts for $hostType")
    Map((for {
      number <- 0 to maxHosts
      host <- Play.current.configuration.getString(s"client.scm.$hostType.host.$number")
    } yield host -> number): _*)
  }

  def urlPrecedent(hostType: String): Map[Int, String] = {
    logger.info(s"Loading all precedents for $hostType")
    readValues(hostType, s"client.scm.$hostType.urlPrecedent")
  }

  private def readValues(hostType: String, key: String): Map[Int, String] = {
    Map((for {
      number <- 0 to maxHosts
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

}