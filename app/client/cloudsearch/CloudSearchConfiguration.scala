package client.cloudsearch

import play.api.Logger
import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
 * @author fbenjamin
 */
trait CloudSearchConfiguration {
  def appsSearchEndpoint: Option[String]
  def appsDocEndpoint: Option[String]
  def repositoriesSearchEndpoint: Option[String]
  def repositoriesDocEndpoint: Option[String]
  def commitsSearchEndpoint: Option[String]
  def commitsDocEndpoint: Option[String]
  def authorsSearchEndpoint: Option[String]
  def authorsDocEndpoint: Option[String]
  def ticketsSearchEndpoint: Option[String]
  def ticketsDocEndpoint: Option[String]
}

class CloudSearchConfigurationImpl extends CloudSearchConfiguration {

  val logger: Logger = Logger { this.getClass }

  lazy val appsSearchEndpoint = loadConfiguration("client.cloudsearch.apps.search.endpoint")
  lazy val appsDocEndpoint = loadConfiguration("client.cloudsearch.apps.doc.endpoint")
  lazy val repositoriesSearchEndpoint = loadConfiguration("client.cloudsearch.repositories.search.endpoint")
  lazy val repositoriesDocEndpoint = loadConfiguration("client.cloudsearch.repositories.doc.endpoint")
  lazy val commitsSearchEndpoint = loadConfiguration("client.cloudsearch.commits.search.endpoint")
  lazy val commitsDocEndpoint = loadConfiguration("client.cloudsearch.commits.doc.endpoint")
  lazy val authorsSearchEndpoint = loadConfiguration("client.cloudsearch.authors.search.endpoint")
  lazy val authorsDocEndpoint = loadConfiguration("client.cloudsearch.authors.doc.endpoint")
  lazy val ticketsSearchEndpoint = loadConfiguration("client.cloudsearch.tickets.search.endpoint")
  lazy val ticketsDocEndpoint = loadConfiguration("client.cloudsearch.tickets.doc.endpoint")

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

