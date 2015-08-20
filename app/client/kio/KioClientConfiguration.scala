package client.kio

/**
 * @author fbenjamin
 */
trait KioClientConfiguration {

  def serviceUrl: String

}

class KioClientConfigurationImpl extends KioClientConfiguration {

  def serviceUrl: String = play.Play.application.configuration.getString("client.kio.service.url")

}

