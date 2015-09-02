package client.kio

import org.scalatest.mock.MockitoSugar
import test.util.MockitoUtils
import org.scalatest.FlatSpec
import client.RequestDispatcher
import play.api.libs.ws.WSRequest
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.TestData
import test.util.FakeResponseData
import scala.concurrent.Future

/**
 * @author fbenjamin
 */
class KioClientTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val dispatcher = mock[RequestDispatcher]
  private val requestHolder = mock[WSRequest]
  private val config = new KioClientConfigurationImpl {
    override def serviceUrl = "thisUrlSucks"
  }
  private val client = new KioClientImpl(dispatcher, config)
  private val oAuthAccessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)

  "KioClient#apps" should "return fully parsed App objects " in {
    val wsResult = createMockedWSResponse(FakeResponseData.kioApps, 200)

    when(dispatcher.requestHolder(config.serviceUrl)).thenReturn(requestHolder)
    when(requestHolder.withHeaders(("Authorization", "Bearer " + oAuthAccessToken.accessToken))).thenReturn(requestHolder)
    when(requestHolder.get()).thenReturn(Future.successful(wsResult))

    val result = client.apps(oAuthAccessToken)

    verify(dispatcher, times(1)).requestHolder(config.serviceUrl)
    verify(requestHolder, times(1)).withHeaders(("Authorization", "Bearer " + oAuthAccessToken.accessToken))
    verify(requestHolder, times(1)).get()

  }

}