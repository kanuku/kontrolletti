package filter

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatest.mock.MockitoSugar
import configuration.OAuthConfiguration
import client.RequestDispatcher
import play.api.inject.bind
import test.util.KontrollettiOneAppPerTestWithOverrides
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import play.api.test._
class OAuth2FilterTest extends PlaySpec with MockitoSugar with KontrollettiOneAppPerTestWithOverrides with BeforeAndAfter {

  private val oauth2Configuration: OAuthConfiguration = mock[OAuthConfiguration]
  private val dispatcher = mock[RequestDispatcher]
  private val swaggerUrl = "/swagger"
  private val assetsUrl = "/assets"
  private val statusUrl = "/status"
  private val specsUrl = "/specs"

  before {
    reset(dispatcher, oauth2Configuration)
  }

  override def overrideModules = {
    Seq(bind[OAuthConfiguration].toInstance(oauth2Configuration),
      bind[RequestDispatcher].toInstance(dispatcher))
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

  }

  def assertNoMocksCalled() = {
    verify(dispatcher, times(0)).requestHolder(anyString())
    verify(oauth2Configuration, times(0)).tokenInfoRequestEndpoint
  }
}
