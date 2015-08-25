package client.cloudsearch

import play.api.Logger
import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
 * @author fbenjamin
 */
trait CloudSearchConfiguration {
  def appsEndpoint: Option[String]
  def repositoriesEndpoint: Option[String]
  def commitsEndpoint: Option[String]
  def authorsEndpoint: Option[String]
  def ticketsEndpoint: Option[String]
}

class CloudSearchConfigurationImpl extends CloudSearchConfiguration {

  val logger: Logger = Logger { this.getClass }

  lazy val appsEndpoint = loadConfiguration("client.cloudsearch.apps.search.endpoint")
  lazy val repositoriesEndpoint = loadConfiguration("client.cloudsearch.repositories.search.endpoint")
  lazy val commitsEndpoint = loadConfiguration("client.cloudsearch.commits.search.endpoint")
  lazy val authorsEndpoint = loadConfiguration("client.cloudsearch.authors.search.endpoint")
  lazy val ticketsEndpoint = loadConfiguration("client.cloudsearch.tickets.search.endpoint")

  private def loadConfiguration(load: => String): Option[String] = {
    Try(play.Play.application.configuration.getString(load)) match {
      case Success(result) =>
        logger.info(s"Loaded endpoint $load with endpoint: $result")
        Option(result)
      case Failure(ex) =>
        logger.error(s"Failed to load endpoint $load with error: " + ex.getMessage)
        None
    }
  }

}

