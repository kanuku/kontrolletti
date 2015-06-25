package service

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import client.SCM
import model.Author
import test.util.MockitoUtils
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import org.scalatest.FunSpec
import client.SCMImpl

/**
 * This class tests the interaction between the Service and the Client.
 * The tests reassure us that the logic in the Service class is well implemented
 * and passed to the client correctly.
 * The client is mocked and therefore not tested here.
 */
class SearchTest extends FlatSpec with OneAppPerSuite with MockitoSugar with MockitoUtils with BeforeAndAfter {

  import test.util.TestUtils._

  val client = mock[SCM]
  val searchWithMockClient: OldSearch = new OldSearchImpl(client)
  val search: Search = new SearchImpl(new SCMImpl())
  val users = List(Author("name", "email", null))

  before {
    reset(client)
  }

  def githubFixture = new {
    val host = "github.com"
    val project = "zalando-bus"
    val repo = "kontrolletti"
    val url = s"https://$host/$project/$repo/"
  }

  def stashFixture = new {
    val host = "stash.zalando.net"
    val project = "DOC"
    val repo = "ci-cd"
    val url = s"https://stash.zalando.net/projects/$project/repos/$repo"
  }

  "committers " should "call the client with parsed github Url params" in {

    val clientResult = mockSuccessfullParsableFutureWSResponse(users,200)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(searchWithMockClient.committers(githubFixture.host,githubFixture.project,githubFixture.repo), Duration("10 seconds"))

    assertEitherIsNotNull(either)
    assertEitherIsRight(either)
    assert(either.right.get == users)

    val hostCap = ArgumentCaptor.forClass(classOf[String])
    val groupCap = ArgumentCaptor.forClass(classOf[String])
    val repoCap = ArgumentCaptor.forClass(classOf[String])

    // Verify the
    verify(client).committers(hostCap.capture(), groupCap.capture(), repoCap.capture());

    assert(hostCap.getValue == githubFixture.host)
    assert(groupCap.getValue == githubFixture.project)
    assert(repoCap.getValue == githubFixture.repo)

  }

  it should "call the client with parsed stash Url params" in {

    val clientResult = mockSuccessfullParsableFutureWSResponse(users,200)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(searchWithMockClient.committers(stashFixture.host,stashFixture.project,stashFixture.repo), Duration("10 seconds"))

    assertEitherIsNotNull(either)
    assertEitherIsRight(either)
    assert(either.right.get == users)

    val hostCap = ArgumentCaptor.forClass(classOf[String])
    val groupCap = ArgumentCaptor.forClass(classOf[String])
    val repoCap = ArgumentCaptor.forClass(classOf[String])

    // Verify the
    verify(client).committers(hostCap.capture(), groupCap.capture(), repoCap.capture());

    assert(hostCap.getValue == stashFixture.host)
    assert(groupCap.getValue == stashFixture.project)
    assert(repoCap.getValue == stashFixture.repo)

  }
  it should "handle Unexpected client-exceptions gracefully" in {

    val clientResult = Future.failed(new RuntimeException("Something bad happened!"))

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(searchWithMockClient.committers(githubFixture.host,githubFixture.project,githubFixture.repo), Duration("10 seconds"))
    val hostCap = ArgumentCaptor.forClass(classOf[String])
    val groupCap = ArgumentCaptor.forClass(classOf[String])
    val repoCap = ArgumentCaptor.forClass(classOf[String])

    // Verify the
    verify(client).committers(hostCap.capture(), groupCap.capture(), repoCap.capture());

    assert(hostCap.getValue == githubFixture.host)
    assert(groupCap.getValue == githubFixture.project)
    assert(repoCap.getValue == githubFixture.repo)

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "An internal error occurred!")

  }

  
  it should "not find the client when the host is empty" in {
    //Start testing
    val either = Await.result(searchWithMockClient.committers("","",""), Duration("10 seconds"))
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "Could not resolve the client for ")
  }
  it should "not find the client when the host is null" in {
    //Start testing.
    val either = Await.result(searchWithMockClient.committers(null,null,null), Duration("10 seconds"))
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "Could not resolve the client for null")
  }
  "normalize" should "normalize github anonymous git-clone-url" in {
    assert(search.normalize("github.com","zalando","kontrolletti") === "/projects/zalando/repos/kontrolletti")
  }
   
  
}