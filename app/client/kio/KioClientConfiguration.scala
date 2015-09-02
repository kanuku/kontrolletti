package client.kio

import javax.inject.Singleton
/**
 * @author fbenjamin
 */
trait KioClientConfiguration {

  def serviceUrl: String

}
@Singleton
class KioClientConfigurationImpl extends KioClientConfiguration {

  def serviceUrl: String = play.Play.application.configuration.getString("client.kio.service.url")

}

