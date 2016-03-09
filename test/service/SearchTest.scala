package service

import scala.{ Left, Right }
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers._
import org.scalatestplus.play.OneAppPerSuite
import com.google.inject.ImplementedBy
import client.RequestDispatcherImpl
import client.scm.{ GithubResolver, SCM, SCMImpl, SCMResolver, StashResolver }
import configuration.GeneralConfiguration
import javax.inject.{ Inject, Singleton }
import model.Link
import play.api.libs.ws.{ WSClient, WSResponse }
import test.util.{ ConfigurableFakeApp, MockitoUtils }
import test.util.TestUtils.{ assertEitherIsLeft, assertEitherIsNotNull, assertEitherIsRight }
import configuration.SCMConfigurationImpl
import test.util.ConfigurationDefaults.SCMConfigurationDefaults._
import client.scm.SCMParser
import client.oauth.OAuth
import client.scm.GithubToJsonParser

/**
 * This class tests the interaction between the Service and the Client(mock).
 */
class SearchTest extends FlatSpec with MockitoSugar with MockitoUtils with OneAppPerSuite with ConfigurableFakeApp with BeforeAndAfter {

  val defaultError = "Something went wrong, check the logs!"
  val host = "github.com"
  val project = "zalando"
  val repository = "kontrolletti"
  val url = s"https://$host/$project/$repository"
  val sourceId = "sourceId"
  val targetId = "targetId"
  val commitId = "commitId"
  val commitJsonStr = """{"sha":"", "commit":{"committer":{"name":"name","email":"email"},"author":{"date":"2016-03-08T14:53:15+0100"},"message":""}}"""
  val repoJsonStr = """{"html_url": "some-url"}"""
  val client = mock[SCM]
  val oauth = mock[OAuth]
  val conf = new SCMConfigurationImpl
  val githubResolver = new GithubResolver(conf)
  val stashResolver = new StashResolver(conf, oauth)
  val stashParser = new GithubToJsonParser(stashResolver)
  val githubParser = new GithubToJsonParser(githubResolver)
  val search: Search = new SearchImpl(client, stashParser, githubParser)
  val response200: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("{}", 200)
  val commitsResponse200: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse("[]", 200)
  val commitResponse200: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(commitJsonStr, 200)
  val repoResponse200: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(repoJsonStr, 200)
  val response404: Future[WSResponse] = mockSuccessfullParsableFutureWSResponse(null, 404)

  implicit override lazy val app = fakeApplication
  before {
    reset(client)
    when(client.resolver("stash.com")).thenReturn(stashResolver)
    when(client.resolver("github.com")).thenReturn(githubResolver)

  }

  "Search#commits" should "return commits when the http-result  is 200 and body is not empty" in {
    when(client.commits(host, project, repository, None, None, 1)).thenReturn(commitsResponse200)
    val result = Await.result(search.commits(host, project, repository, None, None, 1), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(!result.right.get.isEmpty, "Result must not be empty")
    verify(client, times(1)).commits(host, project, repository, None, None, 1)
  }
  it should "return None when the result is 404" in {
    when(client.commits(host, project, repository, None, None, 1)).thenReturn(response404)
    val result = Await.result(search.commits(host, project, repository, None, None, 1), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result.right.get.isEmpty, "Result must be None")
    verify(client, times(1)).commits(host, project, repository, None, None, 1)
  }
  it should "return an error when client throws an Exception" in {
    when(client.commits(host, project, repository, None, None, 1)).thenThrow(new RuntimeException())
    val result = Await.result(search.commits(host, project, repository, None, None, 1), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    verify(client, times(1)).commits(host, project, repository, None, None, 1)
  }

  "Search#commit" should "return a single commit when the http-result  is 200" in {
    when(client.commit(host, project, repository, commitId)).thenReturn(commitResponse200)
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsRight(result)
    // assertEitherIsNotNull(result)
    // assert(result != Right(None), "Result must not be empty")
    verify(client, times(1)).commit(host, project, repository, commitId)
  }
  it should "return None when the result is 404" in {
    when(client.commit(host, project, repository, commitId)).thenReturn(response404)
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsRight(result)
    assertEitherIsNotNull(result)
    assert(result == Right(None), "Result must be None")
    verify(client, times(1)).commit(host, project, repository, commitId)
  }
  it should "return an error when client throws an Exception" in {
    when(client.commit(host, project, repository, commitId)).thenThrow(new RuntimeException())
    val result = Await.result(search.commit(host, project, repository, commitId), Duration("5 seconds"))
    assertEitherIsLeft(result)
    assertEitherIsNotNull(result)
    assert(result == Left(defaultError))
    verify(client, times(1)).commit(host, project, repository, commitId)
  }

  //FIXME! If you use Optional type in the last assertion, the problem should be clear. Needs investigation.
  //TODO: 1st test seems wrong
  "Search#repos" should " return empty result when the http-result is 200 and body is not empty" in {
    when(client.repo(host, project, repository)).thenReturn(repoResponse200)
    val Right(result) = Await.result(search.repo(host, project, repository), Duration("5 seconds"))
    assert(!result.isEmpty, "Result must be empty")
    verify(client, times(1)).repo(host, project, repository)
  }
  it should " return empty list when the result is 404" in {
    when(client.repo(host, project, repository)).thenReturn(response404)
    val Right(result) = Await.result(search.repo(host, project, repository), Duration("5 seconds"))
    assert(result.isEmpty, "Result should be empty")
    verify(client, times(1)).repo(host, project, repository)
  }
  it should " return an error when client throws an exception" in {
    when(client.repo(host, project, repository)).thenThrow(new RuntimeException())
    val Left(result) = Await.result(search.repo(host, project, repository), Duration("5 seconds"))
    assert(result == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).repo(host, project, repository)
  }

  "Search#parse" should "just parse :D " in {
    assert(search.parse(url) == Right((host, project, repository)))
  }

  "Search#normalize" should "normalize the github URL" in {
    val client = new SCMImpl(new RequestDispatcherImpl(mock[WSClient], mock[GeneralConfiguration]), githubResolver, stashResolver)
    val search = new SearchImpl(client, stashParser, githubParser)
    assert(search.normalize(host, project, repository) == url)
  }
  "Search#normalize" should "normalize the stash URL" in {
    val url = "https://stash.com/projects/zalando/repos/kontrolletti/browse"
    val client = new SCMImpl(new RequestDispatcherImpl(mock[WSClient], mock[GeneralConfiguration]), githubResolver, stashResolver)
    val search = new SearchImpl(client, stashParser, githubParser)
    assert(search.normalize("stash.com", project, repository) == url)
  }

  "Search#diff" should "return true when receiving a 200 Response" in {
    val diffLink = "https://github.com/zalando/kontrolletti/compare/sourceId...targetId"
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(diffLink)
    when(client.head(host, diffLink)).thenReturn(response200)
    val Right(result) = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("500 seconds"))
    assert(result == Some(Link(diffLink, null, null, null)), "The Link object should have a href")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(host, diffLink)
  }
  it should "return false when receiving a 404 response" in {
    val diffLink = "https://github.com/zalando/kontrolletti/compare/sourceId...targetId"
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(diffLink)
    when(client.head(host, diffLink)).thenReturn(response404)
    val Right(result) = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assert(result == None, "The result should be None")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(host, diffLink)
  }
  it should "return error when client throws exception" in {
    when(client.diffUrl(host, project, repository, sourceId, targetId)).thenReturn(url)
    when(client.head(host, url)).thenThrow(new RuntimeException())
    val Left(result) = Await.result(search.diff(host, project, repository, sourceId, targetId), Duration("5 seconds"))
    assert(result == defaultError, f"Result should be [$defaultError]")
    verify(client, times(1)).diffUrl(host, project, repository, sourceId, targetId)
    verify(client, times(1)).head(host, url)
  }
  "Search#isRepo" should "return true when http-result is a 200 response" in {
    when(client.checkRepoUrl(host, project, repository)).thenReturn(url)
    when(client.head(host, url)).thenReturn(response200)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assert(result == true, "Result must be true")
    verify(client, times(1)).checkRepoUrl(host, project, repository)
    verify(client, times(1)).head(host, url)
  }
  it should "return false when receiving a 404 response" in {
    when(client.checkRepoUrl(host, project, repository)).thenReturn(url)
    when(client.head(host, url)).thenReturn(response404)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    assert(result == false, "Result must be false")
    verify(client, times(1)).checkRepoUrl(host, project, repository)
    verify(client, times(1)).head(host, url)
  }
  it should "return error when client throws exception" in {
    when(client.checkRepoUrl(host, project, repository)).thenReturn(url)
    when(client.head(host, url)).thenThrow(new RuntimeException())
    val Left(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    result shouldBe "Something went wrong, check the logs!"
  }
  it should "return true if the project is allowed" in {
    when(client.checkRepoUrl(host, project, repository)).thenReturn(url)
    when(client.head(host, url)).thenReturn(response200)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    result shouldBe true
  }
  it should "return false if the result of the url returns a 404" in {
    when(client.checkRepoUrl(host, project, repository)).thenReturn(url)
    when(client.head(host, url)).thenReturn(response404)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    result shouldBe false
  }
  it should "return true if the allowedProjects in the configuration is empty" in {
    val host = "stash.com"
    val project = "kanuku"
    val repoUrl = s"https://$host/$project/kontrolletti"
    when(client.checkRepoUrl(host, project, repository)).thenReturn(repoUrl)
    when(client.head(host, repoUrl)).thenReturn(response200)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    result shouldBe true
  }
  it should "return false if the project is not allowed" in {
    val project = "kanuku"
    val repoUrl = s"https://$host/$project/$repository"
    when(client.checkRepoUrl(host, project, repository)).thenReturn(repoUrl)
    when(client.head(host, repoUrl)).thenReturn(response200)
    val Right(result) = Await.result(search.isRepo(host, project, repository), Duration("5 seconds"))
    result shouldBe false
  }

  override def configuration: Map[String, _] = scmConfigurations
}
