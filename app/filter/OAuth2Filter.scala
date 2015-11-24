package filter

import scala.concurrent.{ ExecutionContext, Future }

import client.oauth.{ OAuth, OAuthTokenInfo }
import configuration.OAuthConfiguration
import javax.inject.Inject
import model.Error
import model.KontrollettiToJsonParser.errorWriter
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{ Filter, RequestHeader, Result, Results }
/**
 * This filter evaluates all paths, except the excluded ones, for a valid access_token.
 * The access_token gets evaluated against the configured OAuth2 Service.
 *
 *
 */
class OAuth2Filter @Inject() (config: OAuthConfiguration, client: OAuth)(implicit ec: ExecutionContext) extends Filter {
  private val missingTokenResult = Future.successful(Results.Unauthorized(Json.toJson(new Error("Unauthorized", 0, "Authentication info is missing"))))
  private val unathorizedResult = Future.successful(Results.Unauthorized(Json.toJson(new Error("Unauthorized", 0, "Validation failed"))))
  private val logger: Logger = Logger { this.getClass }
  private val areInThisPath = (requestHeader: RequestHeader) => (input: String) => requestHeader.path.startsWith(input)
  private val authorizationHeader = "Authorization"
  private val oauthBearerRegex = """^(Bearer ){1,1}"""
  private val oauthTokenRegex = """(.*$){1,1}"""
  private val oauthHeaderValueRegex = s"$oauthBearerRegex$oauthTokenRegex".r

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if (!config.excludedPaths.find(areInThisPath(requestHeader)).isEmpty) {
      nextFilter.apply(requestHeader)
    } else {
      requestHeader.headers.get(authorizationHeader) match {
        case Some(oauthHeaderValueRegex(bearer, token)) =>
          client.tokenInfo(token).flatMap { tokenInfoResult =>
            tokenInfoResult match {
              case Some(OAuthTokenInfo(_, _, _, _, _, expiresIn, accessToken)) if (expiresIn > 0 && accessToken == token) =>
                nextFilter.apply(requestHeader)
              case None =>
                unathorizedResult
            }
          }
        case None =>
          logger.info("Token was not available in the : " + requestHeader.path)
          missingTokenResult
      }
    }
  }

}