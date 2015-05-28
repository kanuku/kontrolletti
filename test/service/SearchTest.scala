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
import test.util.MockitoUtils._

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import org.scalatest.FunSpec

/**
 * This class tests the interaction between the Service and the Client.
 * The tests reassure us that the logic in the Service class is well implemented
 * and passed to the client correctly.
 * The client is mocked and therefore not tested here.
 */
class SearchTest extends  FlatSpec with OneAppPerSuite with MockitoSugar with BeforeAndAfter {


import test.util.TestUtils._

  val client = mock[SCM]
  val search: Search = new SearchImpl(client)
  val users = List(Author("name", "email"))

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

  "Search with a Github repo" should "call the client with parsed values from url" in {

    val clientResult = mockSuccessfullParsableFutureWSResponse(users)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(search.committers(githubFixture.url), Duration("10 seconds"))

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

  "Search with a Stash repo" should "call the client with parsed values from url" in {

    val clientResult = mockSuccessfullParsableFutureWSResponse(users)

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(search.committers(stashFixture.url), Duration("10 seconds"))

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
  "Search" should "handle Unexpected client-exceptions gracefully" in {

    val clientResult = Future.failed(new RuntimeException("Something bad happened!"))

    when(client.committers(anyString, anyString, anyString)).thenReturn(clientResult)

    //Start testing
    val either = Await.result(search.committers(githubFixture.url), Duration("10 seconds"))
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

  "Search" should "never call the client when the url is not parsable" in {
    val url = "asdfasdfasdfaölkajsdf"
    //Start testing    
    val either = Await.result(search.committers(url), Duration("10 seconds"))
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == (s"Could not parse $url"))

  }
  it should "never call the client when the url is empty" in {
    //Start testing
    val either = Await.result(search.committers(""), Duration("10 seconds"))
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "URL is empty")
  }
  it should "never call the client when the url is null" in {
    //Start testing.
    val either = Await.result(search.committers(null), Duration("10 seconds"))
    // Verify the method is never called when
    verify(client, times(0)).committers(anyObject(), anyObject(), anyObject());

    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "URL is null")
  }

}