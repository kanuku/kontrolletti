package test.util

import org.mockito.Mockito._
import org.mockito.Matchers._
import client.RequestDispatcher
import client.oauth.OAuthTokenInfo
import play.api.libs.ws.WSRequest
import configuration.OAuthConfiguration
import play.api.test.FakeHeaders
import play.api.libs.json.Json
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatest.Suite
import scala.concurrent.Future
import client.oauth.OAuthParser._

trait OAuthTestBuilder extends MockitoUtils {
  /**
   * The following mocks need not to be private.
   *  Those mocks should be accessible, for flexibility purposes,
   *  for using it with Guice during the execution of tests.
   *  If necessary of course!
   *
   */
  val oauthConfig = mock[OAuthConfiguration]
  val dispatcher = mock[RequestDispatcher]

  private val requestHolder = mock[WSRequest]
  private val oauthToken = "98266616-5ad5-4326-b3d6-c049ad51831d"
  private val tokenInfoEndpoint = "tokenInfoEndpoint"
  private val oauthTokenInfo = new OAuthTokenInfo("username", None, "password", "/services", "Bearer", 1200, oauthToken)
  private val defaultOauthHeader = FakeHeaders(List(("Authorization" -> oauthToken)))
  private val requestResult = createMockedWSResponse(Json.stringify(Json.toJson(oauthTokenInfo)), 200)

  def recordOAuthBehaviour() = {
    reset(oauthConfig, dispatcher, requestHolder)
    //Make sure you mock the expected Behaviour from the MockedObject
    when(oauthConfig.excludedPaths).thenReturn(Set[String]())
    when(oauthConfig.tokenInfoRequestEndpoint).thenReturn(tokenInfoEndpoint)
    when(dispatcher.requestHolder(anyString())).thenReturn(requestHolder)
    when(requestHolder.withHeaders(("Authorization", "Bearer " + oauthToken))).thenReturn(requestHolder)
    when(requestHolder.get()).thenReturn(Future.successful(requestResult))
  }

  def verifyOAuthBehaviour() = {
    verify(oauthConfig, times(1)).excludedPaths
    verify(oauthConfig, times(1)).tokenInfoRequestEndpoint
    verify(dispatcher, times(1)).requestHolder(tokenInfoEndpoint)
    verify(requestHolder, times(1)).withHeaders(("Authorization", "Bearer " + oauthToken))
    verify(requestHolder, times(1)).get()
  }
}