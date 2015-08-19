package client.kio

import org.scalatest.mock.MockitoSugar
import test.util.MockitoUtils
import org.scalatest.FlatSpec
import client.RequestDispatcher
import play.api.libs.ws.WSRequestHolder
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
 * @author fbenjamin
 */
class KioClientTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val dispatcher = mock[RequestDispatcher]
  private val requestHolder = mock[WSRequestHolder]
  private val client = new KioClientImpl(dispatcher)

  "KioClient#appIds" should " " in {
    when(dispatcher.requestHolder(anyString)).thenReturn(requestHolder)

  }
  "KioClient#apps" should " " in {
    when(dispatcher)
  }

}