package service

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec
import client.SCM
import client.SCMImpl
import client.SCMImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import test.util.MockitoUtils
import client.RequestDispatcherImpl
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
  val commitId = "commitId"
  val client = mock[SCM]
  val search: Search = new SearchImpl(client)

  before {
    reset(client)
  }

  "Search#commits" should "return commits when the result is 200 and body is not empty" in {
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

  "Search#commit" should "return a single commit when the result is 200" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.commit(host, project, repository, commitId)).thenReturn(response)
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).commit(host, project, repository, commitId)
  }
  it should "return None when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(null, 404)
    when(client.commit(host, project, repository, commitId)).thenReturn(response)
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isEmpty, "Result must be None")
    verify(client, times(1)).commit(host, project, repository, commitId)
  }
  it should "return an error when client throws an Exception" in {
    when(client.commit(host, project, repository, commitId)).thenThrow(new RuntimeException())
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError)
    verify(client, times(1)).commit(host, project, repository, commitId)
  }

  "Search#repos" should " return repositories when the result is 200 and body is not empty" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.repo(host, project, repository)).thenReturn(response)
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).repo(host, project, repository)
  }
  it should " return empty list when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(null, 404)
    when(client.repo(host, project, repository)).thenReturn(response)
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isEmpty, "Result should be empty")
    verify(client, times(1)).repo(host, project, repository)
  }
  it should " return an error when client throws an exception" in {
    when(client.repo(host, project, repository)).thenThrow(new RuntimeException())
    val result = Await.result(search.repos(host, project, repository), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).repo(host, project, repository)
  }

  "Search#parse" should "just parse :D " in {
    val result = search.parse(url)
    assert(result.isRight)
    assert(result.right.get == (host, project, repository))
  }

  "Search#normalize" should "normalize the URL" in {
    val client = new SCMImpl(new RequestDispatcherImpl())
    val search = new SearchImpl(client)
    assert(search.normalize(host, project, repository) == url)
  }

  "Search#isRepo" should "return true when receiving a 200 response" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 200)
    when(client.repoUrl(host, project, repository)).thenReturn(url)
    when(client.head(url)).thenReturn(response)
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get, "Result must be true")
    verify(client, times(1)).repoUrl(host, project, repository)
    verify(client, times(1)).head(url)
  }
  it should "return false when receiving a 404 response" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 404)
    when(client.repoUrl(host, project, repository)).thenReturn(url)
    when(client.head(url)).thenReturn(response)
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get, "Result must be false")
    verify(client, times(1)).repoUrl(host, project, repository)
    verify(client, times(1)).head(url)
  }
  it should "return error when client throws exception" in {
    when(client.repoUrl(host, project, repository)).thenReturn(url)
    when(client.head(url)).thenThrow(new RuntimeException())
    val result = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).repoUrl(host, project, repository)
    verify(client, times(1)).head(url)
  }

  "Search#diff" should "return true when receiving a 200 Response" in {
    val diffLink = "https://github.com/zalando/kontrolletti/compare/sourceId...targetId"
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 200)
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(diffLink)
    when(client.head(diffLink)).thenReturn(response)
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isDefined)
    assert(result.right.get.get.href == diffLink, "The Link object should have a href")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(diffLink)
  }
  it should "return false when receiving a 404 response" in {
    val diffLink = "https://github.com/zalando/kontrolletti/compare/sourceId...targetId"
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("", 404)
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(diffLink)
    when(client.head(diffLink)).thenReturn(response)
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.isRight)
    assert(!result.right.get.isDefined, "The result should be None")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(diffLink)
  }
  it should "return error when client throws exception" in {
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(url)
    when(client.head(url)).thenThrow(new RuntimeException())
    val result = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(url)
  }
  "Search#tickets" should " return repositories when the result is not Empty" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
    when(client.tickets(host, project, repository)).thenReturn(response)
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get != null)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).tickets(host, project, repository)
  }
  it should " return empty List when the result is 404" in {
    val response: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 404)
    when(client.tickets(host, project, repository)).thenReturn(response)
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get != null)
    assert(result.right.get.isEmpty, "Result must be empty")
    verify(client, times(1)).tickets(host, project, repository)
  }
  it should " return error when an exception occurs" in {
    when(client.tickets(host, project, repository)).thenThrow(new RuntimeException())
    val result = Await.result(search.tickets(host, project, repository, None, None), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result.isLeft)
    assert(result.left.get == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).tickets(host, project, repository)
  }

}