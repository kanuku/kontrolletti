package client.cloudsearch

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import play.api.Logger
import scala.concurrent.Future
import utility.LoadConfigurationException

/**
 * @author fbenjamin
 */
trait CloudSearchConfiguration {
  def appsSearchEndpoint: Future[String]
  def appsDocEndpoint: Future[String]
  def repositoriesSearchEndpoint: Future[String]
  def repositoriesDocEndpoint: Future[String]
  def commitsSearchEndpoint: Future[String]
  def commitsDocEndpoint: Future[String]
  def authorsSearchEndpoint: Future[String]
  def authorsDocEndpoint: Future[String]
  def ticketsSearchEndpoint: Future[String]
  def ticketsDocEndpoint: Future[String]
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

  private def loadConfiguration(load: => String): Future[String] = {
    Try(play.Play.application.configuration.getString(load)) match {
      case Success(result) =>
        logger.info(s"Loaded endpoint $load with endpoint: $result")

        Future.successful(result)
      case Failure(ex) =>
        val message = s"Failed to load endpoint $load with error: " + ex.getMessage
        logger.error(message)
        Future.failed(new LoadConfigurationException(message))
    }
  }

}

