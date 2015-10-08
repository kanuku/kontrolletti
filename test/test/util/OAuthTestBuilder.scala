package test.util

import scala.concurrent.Future

import org.mockito.Matchers.anyString
import org.mockito.Mockito.{ reset, times, verify, when }

import client.RequestDispatcher
import client.oauth.OAuthClientImpl
import client.oauth.OAuthParser.oAuthOAuthTokenInfoFormatter
import client.oauth.OAuthTokenInfo
import configuration.OAuthConfiguration
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest

trait OAuthTestBuilder extends MockitoUtils {
  /**
   * Non private variables were defined as such for flexibility purposes i.o.w. for usage in the mixed in classes.
   * I.e.: The authorizationHeader should always be used by any fake request, that has OAuth enabled.
   *
   */
  val oauthConfig = mock[OAuthConfiguration]
  val dispatcher = mock[RequestDispatcher]
  val oauthClient = new OAuthClientImpl(dispatcher, oauthConfig)

  private val requestHolder = mock[WSRequest]
  private val oauthToken = "98266616-5ad5-4326-b3d6-c049ad51831d"
  private val tokenInfoEndpoint = "tokenInfoEndpoint"
  private val oauthTokenInfo = new OAuthTokenInfo("username", None, "password", "/services", "Bearer", 1200, oauthToken)
  private val requestResult = createMockedWSResponse(Json.stringify(Json.toJson(oauthTokenInfo)), 200)
  private val oauthEnpointError = createMockedWSResponse("""{"error":"invalid_request","error_description":"Access Token not valid"}""", 400)

  val authorizationHeader = ("Authorization", s"Bearer " + oauthToken)

  def recordOAuthPassAuthenticationBehaviour() = {
    reset(oauthConfig, dispatcher, requestHolder)
    //Make sure you mock the expected Behaviour from the MockedObject
    when(oauthConfig.excludedPaths).thenReturn(Set[String]())
    when(oauthConfig.tokenInfoRequestEndpoint).thenReturn(tokenInfoEndpoint)
    when(dispatcher.requestHolder(anyString())).thenReturn(requestHolder)
    when(requestHolder.withQueryString(("access_token", oauthToken))).thenReturn(requestHolder)
    when(requestHolder.get()).thenReturn(Future.successful(requestResult))
  }

  def verifyOAuthPassAuthenticationBehaviour() = {
    verify(oauthConfig, times(1)).excludedPaths
    verify(oauthConfig, times(1)).tokenInfoRequestEndpoint
    verify(dispatcher, times(1)).requestHolder(tokenInfoEndpoint)
    verify(requestHolder, times(1)).withQueryString(("access_token", oauthToken))
    verify(requestHolder, times(1)).get()
  }

  def recordOAuthFailingTokenAuthenticationBehaviour() = {
    reset(oauthConfig, dispatcher, requestHolder)
    //Make sure you mock the expected Behaviour from the MockedObject
    when(oauthConfig.excludedPaths).thenReturn(Set[String]())
    when(oauthConfig.tokenInfoRequestEndpoint).thenReturn(tokenInfoEndpoint)
    when(dispatcher.requestHolder(anyString())).thenReturn(requestHolder)
    when(requestHolder.withQueryString(("access_token", oauthToken))).thenReturn(requestHolder)
    when(requestHolder.get()).thenReturn(Future.successful(oauthEnpointError))
  }

  def verifyOAuthFailingTokenAuthenticationBehaviour() = {
    verify(oauthConfig, times(1)).excludedPaths
    verify(oauthConfig, times(1)).tokenInfoRequestEndpoint
    verify(dispatcher, times(1)).requestHolder(tokenInfoEndpoint)
    verify(requestHolder, times(1)).withQueryString(("access_token", oauthToken))
    verify(requestHolder, times(1)).get()
  }
}