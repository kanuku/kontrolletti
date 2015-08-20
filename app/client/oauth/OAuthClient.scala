package client.oauth

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import OAuthParser.oAuthAccessTokenReader
import OAuthParser.oAuthClientCredentialReader
import OAuthParser.oAuthUserCredentialReader
import client.RequestDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.ws.WSAuthScheme
import utility.Transformer
/**
 * @author fbenjamin
 */

/** Models */
case class OAuthClientCredential(id: String, secret: String)
case class OAuthUserCredential(username: String, password: String)
case class OAuthAccessToken(tokenType: String, accessToken: String, scope: String, expiresIn: Int)

sealed trait OAuthClient {

  def clientCredentials(): Future[OAuthClientCredential]

  def userCredentials(): Future[OAuthUserCredential]

  def accessToken(): Future[OAuthAccessToken]
}

/**
 * Utility class with for parsers and such
 */
object OAuthParser {

  implicit val oAuthClientCredentialReader: Reads[OAuthClientCredential] = (
    (JsPath \ "client_id").read[String] and
    (JsPath \ "client_secret").read[String])(OAuthClientCredential.apply _)

  implicit val oAuthUserCredentialReader: Reads[OAuthUserCredential] = (
    (JsPath \ "application_username").read[String] and
    (JsPath \ "application_password").read[String])(OAuthUserCredential.apply _)

  implicit val oAuthAccessTokenReader: Reads[OAuthAccessToken] = (
    (JsPath \ "token_type").read[String] and
    (JsPath \ "access_token").read[String] and
    (JsPath \ "scope").read[String] and
    (JsPath \ "expires_in").read[Int])(OAuthAccessToken.apply _)

}

@Singleton
class OAuthClientImpl @Inject() (dispatcher: RequestDispatcher,
                                 config: OAuthConfiguration) extends OAuthClient {
  

  val logger: Logger = Logger { this.getClass }

  private val transformer = Transformer

  def accessToken(client: OAuthClientCredential, serviceUser: OAuthUserCredential): Future[OAuthAccessToken] = {
    logger.info(s"Configuration:$config")
    val result = dispatcher.requestHolder(config.endpointAccessTokenRequest) //
      .withRequestTimeout(config.timeoutRequestClient)
      .withHeaders(("Content-Type", "application/x-www-form-urlencoded"))
      .withAuth(client.id, client.secret, WSAuthScheme.BASIC)
      .withQueryString(("grant_type", "password"))
      .withQueryString(("username", serviceUser.username)) //
      .withQueryString(("password", serviceUser.password)) //
      .withQueryString(("scope", "uid")) //
      .withQueryString(("realm", "/services")) //
      .post("")
    result.flatMap {
      x =>
        logger.info("Oauth-endpoint return http-code: " + x.status)
        transformer.transform(x.body)(OAuthParser.oAuthAccessTokenReader)
    }
  }

  def clientCredentials(): Future[OAuthClientCredential] = get(config.fileNameClientCredentials)(oAuthClientCredentialReader)

  def userCredentials(): Future[OAuthUserCredential] = get(config.fileNameUserCredentials)(oAuthUserCredentialReader)

  def accessToken(): Future[OAuthAccessToken] = {
    logger.info("Getting access token from OAuth service")
    for {
      client: OAuthClientCredential <- clientCredentials()
      user: OAuthUserCredential <- userCredentials()
      result <- accessToken(client, user)
    } yield result
  }
  def get[T](file: String)(implicit rds: Reads[T]): Future[T] = readFile(file).flatMap { x =>
    transformer.parse(x).flatMap { x =>
      transformer.extract2Future(x.validate(rds))
    }
  }

  def readFile(file: String): Future[String] = {
    val fileName: String = config.directoryCredentials.replaceFirst("~", System.getProperty("user.home")) + "/" + file

    logger.info(s"Reading credentials file $fileName")

    Try(scala.io.Source.fromFile(fileName).mkString) match {
      case Success(result) =>
        logger.info("File was read successfully! ")
        Future.successful(result)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        Future.failed(ex)
    }
  }

}

