package client.oauth

import javax.inject.Singleton

/**
 * @author fbenjamin
 */

trait OAuthConfiguration {

  def directoryCredentials: String
  def timeoutRequestClient: Int
  def fileNameClientCredentials: String
  def fileNameUserCredentials: String
  def endpointAccessTokenRequest: String

}
@Singleton
class OAuthConfigurationImpl extends OAuthConfiguration {
  def directoryCredentials = play.Play.application.configuration.getString("client.oauth.credentials.dir")
  def timeoutRequestClient = play.Play.application.configuration.getInt("client.oauth.request.timeout.ms")
  def fileNameClientCredentials: String = play.Play.application.configuration.getString("client.oauth.client.credentials.file")
  def fileNameUserCredentials: String = play.Play.application.configuration.getString("client.oauth.service.credentials.file")
  def endpointAccessTokenRequest: String = play.Play.application.configuration.getString("client.oauth.token.service.url")
}