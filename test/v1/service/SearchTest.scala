package v1.service

import scala.concurrent.Future
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import v1.client.SCM
import v1.test.util.MockitoUtils._
import v1.model.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SearchTest extends FlatSpec with MockitoSugar with BeforeAndAfter {

  val client = mock[SCM]
  val search: Search = new SearchImpl(client)
  val group = "zalando-bus"
  val repo = "kontrolletti"
  val url = s"https://git-hub.com:8080/$group/$repo/"
  val users = List(User("login", 1, 2, "http://something.de"))

  before {
    reset(client)
  }

  "Search" should
    "call the client with parsed values from url" in {

      val clientResult = mockFutureWsResponse(users, success = true)

      when(client.committersFrom(anyString, anyString)).thenReturn(clientResult)

      //Start testing
      val result = search.users(url)
      val groupCap = ArgumentCaptor.forClass(classOf[String])
      val repoCap = ArgumentCaptor.forClass(classOf[String])

      // Verify the
      verify(client).committersFrom(groupCap.capture(), repoCap.capture());

      assert(groupCap.getValue == group)
      assert(repoCap.getValue == repo)

      assert(result != null)
      result.map { rep =>
        assert(rep == users)
      }
    }
  "Search" should
    "handle failed Futures returned by the client" in {

      val clientResult = mockFutureWsResponse(users, success = false)

      when(client.committersFrom(anyString, anyString)).thenReturn(clientResult)

      //Start testing
      val result = search.users(url)
      val groupCap = ArgumentCaptor.forClass(classOf[String])
      val repoCap = ArgumentCaptor.forClass(classOf[String])

      // Verify the
      verify(client).committersFrom(groupCap.capture(), repoCap.capture());
      assert(groupCap.getValue == group)
      assert(repoCap.getValue == repo)
      result.map { rep =>
        assert(rep == users)
      }
    }
  
  "Search" should
    "never call the client when the url is not parsable" in {
      //Start testing
      val result = search.users("")
      // Verify the method is never called when
      verify(client, times(0)).committersFrom(anyObject(), anyObject());
    }

}