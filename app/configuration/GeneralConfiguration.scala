package configuration

import javax.inject.Singleton
import play.api.Play

/**
 * @author fbenjamin
 */
trait GeneralConfiguration {

  /**
   * Endpoints for fetching the list of Registered applications in KIO.
   * @return endpoint
   */
  def kioServiceAppsEndpoint: String
  /**
   *  Timeout for outgoing connections made by RequestDispatcher.
   *  @return timeout the timeout in milliseconds
   *  @see RequestDispatcher
   */
  def defaultClientTimeout: Int

  /**
   * Github hostname which can be used for creating a ticket url-reference to github issues.
   * from a commit message.
   *
   * @return hostname
   * @see TicketParser
   */
  def ticketReferenceGithubHost: String
  /**
   * Github-Enterprise hostname which can be used for creating a ticket url-reference to github-enterprise issues.
   * from a commit message.
   *
   * @return hostname
   * @see TicketParser
   */

  def ticketReferenceGithubEnterpriseHost: String
  /**
   * Jiral url for browsing tickets which can be used for creating a ticket url-reference to jira tickets.
   * from a commit message.
   *
   * @return hostname
   * @see TicketParser
   */
  def ticketReferenceJiraBrowseUrl: String
}

trait ConfigurationDefaults {
  def get[T](input: Option[T]): T = input match {
    case Some(result) =>
      result
    case None => throw new IllegalStateException("Configuration could not be found/loaded!!")
  }
}

@Singleton
class GeneralConfigurationImpl extends GeneralConfiguration with ConfigurationDefaults {
  def defaultClientTimeout: Int = get(Play.current.configuration.getInt("default.client.timeout"))
  def kioServiceAppsEndpoint: String = get(Play.current.configuration.getString("client.kio.service.apps.endpoint"))
  def ticketReferenceGithubHost: String = get(Play.current.configuration.getString("ticket.reference.github.host"))
  def ticketReferenceGithubEnterpriseHost: String = get(Play.current.configuration.getString("ticket.reference.github-enterprise.host"))
  def ticketReferenceJiraBrowseUrl: String = get(Play.current.configuration.getString("ticket.reference.jira.tickets.url"))

}