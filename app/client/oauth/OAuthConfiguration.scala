package client.oauth

import javax.inject.Singleton
import com.google.inject.ImplementedBy

/**
 * @author fbenjamin
 */
@ImplementedBy(classOf[OAuthConfigurationImpl])
trait OAuthConfiguration {
  def credentialsDirectory: String
  def requestClientTimeout: Int
  def clientCredentialsFilename: String
  def userCredentialsFileName: String
  def accessTokenRequestEndpoint: String

  override def toString = s"([credentials-directory=$credentialsDirectory], [request-client-timeout=$requestClientTimeout], [client-credentials-filename=$clientCredentialsFilename], [user-credentials-filename=$userCredentialsFileName],[access-token-request-endpoint=$accessTokenRequestEndpoint])"

}

@Singleton
class OAuthConfigurationImpl extends OAuthConfiguration {
  def credentialsDirectory = play.Play.application.configuration.getString("client.oauth.credentials.dir")
  def requestClientTimeout = play.Play.application.configuration.getInt("client.oauth.request.timeout.ms")
  def clientCredentialsFilename: String = play.Play.application.configuration.getString("client.oauth.client.credentials.file")
  def userCredentialsFileName: String = play.Play.application.configuration.getString("client.oauth.service.credentials.file")
  def accessTokenRequestEndpoint: String = play.Play.application.configuration.getString("client.oauth.token.service.url")
}