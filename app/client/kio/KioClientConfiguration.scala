package client.kio

/**
 * @author fbenjamin
 */
trait KioClientConfiguration {

  def serviceUrlKio: String

}

class KioClientConfigurationImpl extends KioClientConfiguration {

  def serviceUrlKio: String = play.Play.application.configuration.getString("client.kio.service.url")

}

