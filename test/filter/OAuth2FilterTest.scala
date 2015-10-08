package filter

import org.mockito.Matchers.anyString
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec

import client.RequestDispatcher
import client.oauth.OAuth
import configuration.OAuthConfiguration
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.test.FakeRequest
import play.api.test.Helpers.{ GET, NOT_FOUND, OK, UNAUTHORIZED, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty }
import test.util.{ KontrollettiOneAppPerTestWithOverrides, OAuthTestBuilder }
class OAuth2FilterTest extends PlaySpec with MockitoSugar with KontrollettiOneAppPerTestWithOverrides with BeforeAndAfter with OAuthTestBuilder {

  private val swaggerUrl = "/swagger"
  private val assetsUrl = "/assets"
  private val statusUrl = "/status"
  private val specsUrl = "/specs"
  private val excludes = Set(swaggerUrl, assetsUrl, statusUrl, specsUrl)
  before {
    reset(dispatcher, oauthConfig)
    when(oauthConfig.excludedPaths).thenReturn(excludes)
  }

  override def overrideModules = {
    Seq(bind[OAuthConfiguration].toInstance(oauthConfig),
      bind[RequestDispatcher].toInstance(dispatcher),
      bind[OAuth].toInstance(oauthClient))
  }

  "OAuth2ServiceFilter" should {
    "not check for Authorization on the /swagger endpoint" in {
      val result = route(FakeRequest(GET, swaggerUrl)).get
      status(result) mustEqual OK
      assertNoMocksCalled()
    }
    "not check for Authorization on the /assets endpoint" in {
      val result = route(FakeRequest(GET, assetsUrl)).get
      status(result) mustEqual NOT_FOUND
      assertNoMocksCalled()
    }
    "not check for Authorization on the /status endpoint" in {
      val result = route(FakeRequest(GET, statusUrl)).get
      status(result) mustEqual OK
      assertNoMocksCalled()
    }
    "not check for Authorization on the /specs endpoint" in {
      val result = route(FakeRequest(GET, specsUrl)).get
      status(result) mustEqual OK
      assertNoMocksCalled()
    }
    "Fail on /api endpoint with no Authorization header at all" in {
      val result = route(FakeRequest(GET, "/api")).get
      status(result) mustEqual UNAUTHORIZED
      assertNoMocksCalled()
    }
    "Fail on /api endpoint with an invalid Authorization header" in {
      val tokenInfoEndpoint = "TokenInfoEndpoint"
      recordOAuthFailingTokenAuthenticationBehaviour()

      val result = route(FakeRequest(GET, "/api").withHeaders(authorizationHeader)).get
      status(result) mustEqual UNAUTHORIZED

      verifyOAuthFailingTokenAuthenticationBehaviour
    }

  }

  def assertNoMocksCalled() = {
    verify(dispatcher, times(0)).requestHolder(anyString())
    verify(oauthConfig, times(0)).tokenInfoRequestEndpoint
    verify(oauthConfig, times(1)).excludedPaths
  }
}
