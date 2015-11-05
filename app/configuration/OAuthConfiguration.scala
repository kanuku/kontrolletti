package configuration

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import play.api.Logger
import play.api.Play
import scala.collection.JavaConversions._

/**
 * @author fbenjamin
 */

trait OAuthConfiguration {

  def credentialsDirectory: String
  def requestClientTimeout: Int
  def clientCredentialsFilename: String
  def userCredentialsFileName: String
  def accessTokenRequestEndpoint: String
  def tokenInfoRequestEndpoint: String
  def excludedPaths: Set[String]
  override def toString = s"([credentials-directory=$credentialsDirectory], [request-client-timeout=$requestClientTimeout], [client-credentials-filename=$clientCredentialsFilename], [user-credentials-filename=$userCredentialsFileName],[access-token-request-endpoint=$accessTokenRequestEndpoint])"
}

@Singleton
class OAuthConfigurationImpl extends OAuthConfiguration with ConfigurationDefaults {

  val logger: Logger = Logger { this.getClass }

  def credentialsDirectory = get(Play.current.configuration.getString("client.oauth.credentials.dir"))
  def requestClientTimeout = get(Play.current.configuration.getInt("client.oauth.request.timeout.ms"))
  def clientCredentialsFilename: String = get(Play.current.configuration.getString("client.oauth.client.credentials.file"))
  def userCredentialsFileName: String = get(Play.current.configuration.getString("client.oauth.user.credentials.file"))
  def accessTokenRequestEndpoint: String = get(Play.current.configuration.getString("client.oauth.access.token.service.url"))
  def tokenInfoRequestEndpoint: String = get(Play.current.configuration.getString("client.oauth.token.info.service.url"))
  def excludedPaths: Set[String] = get(Play.current.configuration.getStringList("service.oauth.exclude.paths")).toSet

}