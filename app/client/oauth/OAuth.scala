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
import configuration.OAuthConfiguration
import play.api.libs.json.Format
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
 * @author fbenjamin
 */

/** Models */
case class OAuthClientCredential(id: String, secret: String)
case class OAuthUserCredential(username: String, password: String)
case class OAuthAccessToken(tokenType: String, accessToken: String, scope: String, expiresIn: Int)
case class OAuthTokenInfo(uid: String, scope: Option[List[String]], //
                          grantType: String, realm: String, //
                          tokenType: String, expiresIn: Int, accessToken: String)
case class OAuthClientException(message: String) extends Exception(message)

sealed trait OAuth {

  def clientCredentials(): Future[OAuthClientCredential]

  def userCredentials(): Future[OAuthUserCredential]

  def accessToken(): Future[OAuthAccessToken]

  def tokenInfo(token: String): Future[Option[OAuthTokenInfo]]
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

  implicit val oAuthOAuthTokenInfoFormatter: Format[OAuthTokenInfo] = (
    (JsPath \ "uid").format[String] and
    (JsPath \ "scope").formatNullable[List[String]] and
    (JsPath \ "grant_type").format[String] and
    (JsPath \ "realm").format[String] and
    (JsPath \ "token_type").format[String] and
    (JsPath \ "expires_in").format[Int] and
    (JsPath \ "access_token").format[String])(OAuthTokenInfo.apply, unlift(OAuthTokenInfo.unapply))

}

@Singleton
class OAuthClientImpl @Inject() (dispatcher: RequestDispatcher,
                                 config: OAuthConfiguration) extends OAuth {

  val logger: Logger = Logger { this.getClass }

  private val transformer = Transformer

  def accessToken(client: OAuthClientCredential, serviceUser: OAuthUserCredential): Future[OAuthAccessToken] = {
    logger.info("OAuth endpoint:" + config.accessTokenRequestEndpoint)
    val result = dispatcher.requestHolder(config.accessTokenRequestEndpoint) //
      .withRequestTimeout(config.requestClientTimeout)
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
        logger.info("Oauth-endpoint returned http-code: " + x.status)
        if (x.status == 200)
          transformer.parse2Future(x.body).flatMap(transformer.deserialize2Future(_)(OAuthParser.oAuthAccessTokenReader))
        else
          Future.failed(new OAuthClientException("Oauth server responded with an unexpected http-code:" + x.status))
    }
  }

  def clientCredentials(): Future[OAuthClientCredential] = get(config.clientCredentialsFilename)(oAuthClientCredentialReader)

  def userCredentials(): Future[OAuthUserCredential] = get(config.userCredentialsFileName)(oAuthUserCredentialReader)

  def accessToken(): Future[OAuthAccessToken] = {
    logger.info(s"Getting access token from OAuth service with config $config")
    for {
      client: OAuthClientCredential <- clientCredentials()
      user: OAuthUserCredential <- userCredentials()
      result <- accessToken(client, user)
    } yield result
  }
  def get[T](file: String)(implicit rds: Reads[T]): Future[T] = readFile(file).flatMap { x =>
    transformer.parse2Future(x).flatMap { x =>
      transformer.deserialize2Future(x)(rds)
    }
  }

  def readFile(file: String): Future[String] = {
    logger.info(s"Reading file: $file")
    val fileName: String = config.credentialsDirectory.replaceFirst("~", System.getProperty("user.home")) + "/" + file

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

  def tokenInfo(token: String): Future[Option[OAuthTokenInfo]] = ???

}
