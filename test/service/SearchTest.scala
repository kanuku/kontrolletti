package service

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec
import client.SCM
import client.SCMImpl
import model.Author
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import test.util.MockitoUtils
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import client.SCMImpl
/**
 * This class tests the interaction between the Service and the Client(mock).
 */
class SearchTest extends FlatSpec with OneAppPerTest with MockitoSugar with MockitoUtils with BeforeAndAfter {

  import test.util.TestUtils._

  val defaultError = "Something went wrong, check the logs!"
  val host = "github.com"
  val project = "zalando-bus"
  val repository = "kontrolletti"
  val url = s"https://github.com/zalando-bus/kontrolletti"
  val sourceId = "sourceId"
  val targetId = "targetId"
  val client = mock[SCM]
  val search: Search = new SearchImpl(client)

  before {
    reset(client)
  }

  "commits" should "return commits when the result is 200 and body is not empty" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.commits(host, project, repository, None, None)).thenReturn(response)
    val result = Await.result(search.commits(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).commits(host, project, repository, None, None)
  }
  it should "return None when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(null, 404)
    when(client.commits(host, project, repository, None, None)).thenReturn(response)
    val result = Await.result(search.commits(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isEmpty, "Result must be None")
    verify(client, times(1)).commits(host, project, repository, None, None)
  }
  it should "return an error when client throws an Exception" in {
    when(client.commits(host, project, repository, None, None)).thenThrow(new RuntimeException())
    val result = Await.result(search.commits(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError)
    verify(client, times(1)).commits(host, project, repository, None, None)
  }

  "repos" should " return repositories when the result is 200 and body is not empty" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.repos(host, project, repository)).thenReturn(response)
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).repos(host, project, repository)
  }
  it should " return empty list when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(null, 404)
    when(client.repos(host, project, repository)).thenReturn(response)
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isEmpty, "Result should be empty")
    verify(client, times(1)).repos(host, project, repository)
  }
  it should " return an error when client throws an exception" in {
    when(client.repos(host, project, repository)).thenThrow(new RuntimeException())
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).repos(host, project, repository)
  }

  "parse" should "just parse :D " in {
    val result = search.parse(url)
    assert(result.isRight)
    assert(result.right.get == (host, project, repository))
  }

  "normalize" should "normalize the URL" in {
    val client = new SCMImpl()
    val search = new SearchImpl(client)
    assert(search.normalize(host, project, repository) == url)
  }

  "isRepo" should "return true when receiving a 200 response" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 200)
    when(client.isRepo(host, project, repository)).thenReturn(response)
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get, "Result must be true")
    verify(client, times(1)).isRepo(host, project, repository)
  }
  it should "return false when receiving a 404 response" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 404)
    when(client.isRepo(host, project, repository)).thenReturn(response)
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get, "Result must be false")
    verify(client, times(1)).isRepo(host, project, repository)
  }
  it should "return error when client throws exception" in {
    when(client.isRepo(host, project, repository)).thenThrow(new RuntimeException())
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).isRepo(host, project, repository)
  }

  "diff" should "return true when receiving a 200 Response" in {
    val diffLink = "https://github.com/zalando/kontrolletti/compare/sourceId...targetId"
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 200)
    when(client.isDiff(host, project, repository)).thenReturn(response)
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isDefined)
    assert(result.right.get.get.href == diffLink, "The Link object should have a href")
    verify(client, times(1)).isDiff(host, project, repository)
  }
  it should "return false when receiving a 404 response" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 404)
    when(client.isDiff(host, project, repository)).thenReturn(response)
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isDefined)
    assert(!result.right.get.isDefined, "The result should be None")
    verify(client, times(1)).isDiff(host, project, repository)
  }
  it should "return error when client throws exception" in {
    when(client.isDiff(host, project, repository)).thenThrow(new RuntimeException())
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).isDiff(host, project, repository)
  }
  "ticket" should " return repositories when the result is not Empty" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.tickets(host, project, repository, None, None)).thenReturn(response)
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get != null)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).tickets(host, project, repository, None, None)
  }
  it should " return empty List when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 404)
    when(client.tickets(host, project, repository, None, None)).thenReturn(response)
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get != null)
    assert(result.right.get.isEmpty, "Result must be empty")
    verify(client, times(1)).tickets(host, project, repository, None, None)
  }
  it should " return error when an exception occurs" in {
    when(client.tickets(host, project, repository, None, None)).thenThrow(new RuntimeException())
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.isLeft)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).tickets(host, project, repository, None, None)
  }

}