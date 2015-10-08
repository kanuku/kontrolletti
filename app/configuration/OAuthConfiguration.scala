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
class OAuthConfigurationImpl extends OAuthConfiguration {

  val logger: Logger = Logger { this.getClass }

  def credentialsDirectory = Play.current.configuration.getString("client.oauth.credentials.dir").get
  def requestClientTimeout = Play.current.configuration.getInt("client.oauth.request.timeout.ms").get
  def clientCredentialsFilename: String = Play.current.configuration.getString("client.oauth.client.credentials.file").get
  def userCredentialsFileName: String = Play.current.configuration.getString("client.oauth.user.credentials.file").get
  def accessTokenRequestEndpoint: String = Play.current.configuration.getString("client.oauth.access.token.service.url").get
  def tokenInfoRequestEndpoint: String = Play.current.configuration.getString("client.oauth.token.info.service.url").get
  def excludedPaths: Set[String] = Play.current.configuration.getStringList("service.oauth.exclude.paths").map(_.toList.toSet).get

}