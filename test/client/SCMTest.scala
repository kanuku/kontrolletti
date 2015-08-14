package client

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.mockito.Mockito._
import org.mockito.Matchers._
import test.util.MockitoUtils
import org.scalatest.BeforeAndAfter
import play.api.libs.ws.WSRequestHolder
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import org.mockito.ArgumentCaptor
import client.scm.SCMImpl

/**
 * The tests in this class will assure:
 * 1. The dispatcher is called with the right URL.
 * 2. The result from the dispatcher call is returned(unchanged) to the caller.
 *
 *
 */
class SCMTest extends FlatSpec with OneAppPerSuite with MockitoSugar with MockitoUtils with BeforeAndAfter {

  val mockedRequestHolder = mock[WSRequestHolder]
  val mockedDispatcher = mock[RequestDispatcher]
  val mockedWSResponse = mock[WSResponse]
  val mockedResponse = mockSuccessfullParsableFutureWSResponse(mockedWSResponse, 200)
  val client = new SCMImpl(mockedDispatcher)

  val github = "github.com"
  val stash = "stash.zalando.net"
  val project = "project"
  val repository = "repository"
  val since = Some("since")
  val until = Some("until")
  val id = "id"
  val source = "source"
  val target = "target"

  before {
    reset(mockedRequestHolder)
    reset(mockedDispatcher)
    reset(mockedWSResponse)
  }

  "SCM#commits" should "request commits from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits"
    testGET(url, client.commits(github, project, repository, since, until))
  }
  it should "request commits from stash API" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository/commits"
    testGET(url, client.commits(stash, project, repository, since, until))
  }

  "SCM#commit" should "request a single commit from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits/$id"
    testGET(url, client.commit(github, project, repository, id))
  }
  it should "request a single commit from stash API" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository/commits/$id"
    testGET(url, client.commit(stash, project, repository, id))
  }

  "SCM#repo" should "request a single repository from github API" in {
    val url = s"https://api.$github/repos/$project/$repository"
    testGET(url, client.repo(github, project, repository))
  }
  it should "request a single repository from stash API" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository"
    testGET(url, client.repo(stash, project, repository))
  }

  "SCM#tickets" should "request a commit from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits"
    testGET(url, client.tickets(github, project, repository))
  }
  it should "request a commit from stash API " in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository/commits"
    testGET(url, client.tickets(stash, project, repository))
  }

  "SCM#repoUrl" should "return a repository-url for github API" in {
    val url = s"https://$github/$project/$repository"
    val result = client.repoUrl(github, project, repository)
    assert(result == url)
  }
  it should "return a repository-url for stash API" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository"
    val result = client.repoUrl(stash, project, repository)
    assert(result == url)
  }

  "SCM#diffUrl" should "return a diffUrl for github frontend" in {
    val url = s"https://$github/$project/$repository/compare/$source...$target"
    val result = client.diffUrl(github, project, repository, source, target)
    assert(result == url)
  }
  it should "return a diffUrl for stash frontend" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository/compare/commits?from=$source&to=$target"
    val result = client.diffUrl(stash, project, repository, source, target)
    assert(result == url)
  }

  "SCM#head" should "GET github url" in {
    val url = s"Test"
    when(mockedDispatcher.requestHolder(anyString())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withHeaders(any())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.head()).thenReturn(mockedResponse)
    val result = client.head(github,url)
    assert(result == mockedResponse)
    val urlCap = ArgumentCaptor.forClass(classOf[String])
    verify(mockedDispatcher, times(1)).requestHolder(urlCap.capture())
    assert(url == urlCap.getValue)
  }
  "SCM#head" should "HEAD stash url" in {
	  val url = s"Test"
			  when(mockedDispatcher.requestHolder(anyString())).thenReturn(mockedRequestHolder)
        when(mockedRequestHolder.withHeaders(any())).thenReturn(mockedRequestHolder)
			  when(mockedRequestHolder.get()).thenReturn(mockedResponse)
			  val result = client.get(stash,url)
			  assert(result == mockedResponse)
			  val urlCap = ArgumentCaptor.forClass(classOf[String])
			  verify(mockedDispatcher, times(1)).requestHolder(urlCap.capture())
			  assert(url == urlCap.getValue)
  }

  def testGET(url: String, call: => Future[WSResponse]) = {
    when(mockedDispatcher.requestHolder(anyString())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withHeaders(any())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.get()).thenReturn(mockedResponse)
    val result = call
    assert(result == mockedResponse)
    val urlCap = ArgumentCaptor.forClass(classOf[String])
    val headerCap = ArgumentCaptor.forClass(classOf[String])
    verify(mockedDispatcher, times(1)).requestHolder(urlCap.capture())
    assert(url == urlCap.getValue)
  }
}