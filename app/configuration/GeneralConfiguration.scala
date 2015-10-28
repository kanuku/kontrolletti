package configuration
import javax.inject.Singleton
import play.api.Play
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

@Singleton
class GeneralConfigurationImpl extends GeneralConfiguration {
  def defaultClientTimeout: Int = Play.current.configuration.getInt("default.client.timeout").get
  def kioServiceAppsEndpoint: String = Play.current.configuration.getString("client.kio.service.apps.endpoint").get
  def ticketReferenceGithubHost: String = Play.current.configuration.getString("ticket.reference.github.host").get
  def ticketReferenceGithubEnterpriseHost: String = Play.current.configuration.getString("ticket.reference.github-enterprise.host").get
  def ticketReferenceJiraBrowseUrl: String = Play.current.configuration.getString("ticket.reference.jira.tickets.url").get

}