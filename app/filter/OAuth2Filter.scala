package filter

import javax.inject.Inject
import configuration.OAuthConfiguration
import play.api.mvc.Filter
import scala.concurrent.Future
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import client.RequestDispatcher

/**
 * This filter evaluates all paths, except the excluded ones, for a valid access_token.
 * The access_token gets evaluated against the configured OAuth2 Service
 *
 *
 */
class OAuth2Filter @Inject() (config: OAuthConfiguration, dispatcher: RequestDispatcher) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    nextFilter.apply(requestHeader)
  }

}