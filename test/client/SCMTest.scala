package client

import org.scalatest._
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

import play.api.libs.ws.WSResponse
import test.util.MockitoUtils
import play.api.libs.ws.WS
import play.api.libs.ws.WSRequestHolder

import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._

import org.scalatestplus.play.OneAppPerSuite

class SCMTest extends FlatSpec with OneAppPerSuite with MockitoSugar with MockitoUtils {
  val host = "github.com"
  val group = "kanuku"
  val repo = "misc"

  withFakeApplication {
    "SCM.commits with github domain" should
      "use the GithubResolver" in {

        val method = mock[(String) => WSRequestHolder]
        val requestHolder = mock[WSRequestHolder]
        val response = mockSuccessfullParsableFutureWSResponse(mock[WSResponse], 200)
        val client: SCM = createClient(method)

        //Record
        when(method.apply(anyString())).thenReturn(requestHolder)
        when(requestHolder.withHeaders(anyString -> "")).thenReturn(requestHolder)
        when(requestHolder.get).thenReturn(response)

        // Start testing
        val result = client.committers(host, group, repo)

        val urlCap = ArgumentCaptor.forClass(classOf[String])

        verify(method, times(1)).apply(urlCap.capture())

        //Verfiy
        assert(urlCap.getValue == GithubResolver.contributors(host, group, repo), "Url is not correct");
        assert(result == response, "Client should return the mocked response")

      }

    "SCM.resolver" should "return the github-client when issued with a github domain" in {

      val client = new SCMImpl()
      val resolver = client.resolver("github.com").get
      assert(resolver != null)
      assert(resolver.isCompatible("github.com"))

    }

    it should "return the stash-client when issued with a stash domain" in {
      val client = new SCMImpl()
      val resolver = client.resolver("stash.zalando.net").get
      assert(resolver != null)
      assert(resolver.isCompatible("stash.zalando.net"))

    }

    it should "throw an exception when issued with a unknown domain" in {
      val client = new SCMImpl()
      val url = "stash.my.pizza"
      val thrown = intercept[IllegalStateException] {
        val resolver = client.resolver(url)
      }
      assert(thrown.getMessage === s"Could not resolve SCM context for $url")

    }
  }

}