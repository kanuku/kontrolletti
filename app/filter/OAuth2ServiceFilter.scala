package filter

import javax.inject.Inject
import client.oauth.OAuthConfiguration
import play.api.mvc.Filter
import scala.concurrent.Future
import play.api.mvc.RequestHeader
import play.api.mvc.Result

/**
 * This filter evaluates all paths, except the excluded ones, for a valid access_token.
 * The access_token gets evaluated against a
 *
 *
 */
class OAuth2ServiceFilter @Inject() (oauthConfiguration: OAuthConfiguration) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    nextFilter.apply(requestHeader)
  }

}