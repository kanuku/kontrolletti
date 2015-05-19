package v1.client

import org.scalatest._
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import play.api.libs.ws.WSResponse
import v1.test.util.MockitoUtils._
import play.api.libs.ws.WS
import play.api.libs.ws.WSRequestHolder
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import v1.test.util.MockitoUtils._

class GithubTest extends FlatSpec with MockitoSugar {
  val host= "github.com"
  val group = "kanuku"
  val repo = "misc"

  "Github -> committersFrom" should
    "uses the parameters in the url" in {
      withFakeApplication {

        val method = mock[(String) => WSRequestHolder]
        val requestHolder = mock[WSRequestHolder]
        val response = mockFutureWSResponse(mock[WSResponse], true)
        val client: SCMClient = createClient(method)

        //Record
        when(method.apply(anyString())).thenReturn(requestHolder)
        when(requestHolder.withHeaders(anyString -> "")).thenReturn(requestHolder)
        when(requestHolder.get).thenReturn(response)

        // Start testing
        val result = client.committers(host, group, repo)

        val urlCap = ArgumentCaptor.forClass(classOf[String])
        
        verify(method,times(1)).apply(urlCap.capture())
        
        //Verfiy
        assert(urlCap.getValue==GithubResolver.contributors(group, repo),"Url is not correct");
        assert(result == response, "Client should return the mocked response")

      }
    }
}