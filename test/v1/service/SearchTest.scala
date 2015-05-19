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
import v1.test.util.MockitoUtils._
import v1.model.Author
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import v1.client.SCMClient

/**
 * This class tests the interaction between the Service and the Client.
 * The tests reassure us that the logic in the Service class is well implemented
 * and passed to the client correctly.
 * The client is mocked and therefore not tested here.
 */
class SearchTest extends FlatSpec with MockitoSugar with BeforeAndAfter {

  val client = mock[SCMClient]
  val search: Search = new SearchImpl(client)
  val host = "git-hub.com:8080"
  val group = "zalando-bus"
  val repo = "kontrolletti"
  val url = s"https://$host/$group/$repo/"
  val users = List(Author("name", "email"))

  before {
    reset(client)
  }

  "Search" should "call the client with parsed values from url" in {

    val clientResult = mockFutureWSResponse(users, success = true)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val result = search.committers(url)
    val hostCap = ArgumentCaptor.forClass(classOf[String])
    val groupCap = ArgumentCaptor.forClass(classOf[String])
    val repoCap = ArgumentCaptor.forClass(classOf[String])

    // Verify the
    verify(client).committers(hostCap.capture(), groupCap.capture(), repoCap.capture());

    assert(hostCap.getValue == host)
    assert(groupCap.getValue == group)
    assert(repoCap.getValue == repo)

    assert(result != null)
    result.map { rep =>
      assert(rep == users)
    }
  }
  it should "handle failed Futures returned by the client" in {

    val clientResult = mockFutureWSResponse(users, success = false)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val result = search.committers(url)
    val hostCap = ArgumentCaptor.forClass(classOf[String])
    val groupCap = ArgumentCaptor.forClass(classOf[String])
    val repoCap = ArgumentCaptor.forClass(classOf[String])

    // Verify the
    verify(client).committers(hostCap.capture(), groupCap.capture(), repoCap.capture());

    assert(hostCap.getValue == host)
    assert(groupCap.getValue == group)
    assert(repoCap.getValue == repo)
    result.map { rep =>
      assert(rep == users)
    }
  }

  it should "never call the client when the url is not parsable" in {
    //Start testing
    val result = search.committers("asdfasdfasdfaölkajsdf")
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());
  }
  it should "never call the client when the url is empty" in {
    //Start testing
    val result = search.committers("")
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());
  }
  it should "never call the client when the url is null" in {
    //Start testing
    val result = search.committers(null)
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());
  }

}