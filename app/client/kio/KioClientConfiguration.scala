package client.kio

import com.google.inject.ImplementedBy

/**
 * @author fbenjamin
 */
@ImplementedBy(classOf[KioClientConfigurationImpl])
trait KioClientConfiguration {

  def serviceUrl: String

}

class KioClientConfigurationImpl extends KioClientConfiguration {

  def serviceUrl: String = play.Play.application.configuration.getString("client.kio.service.url")

}

