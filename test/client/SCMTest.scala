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

/**
 * The tests in this class will assure:
 * 1. The dispatcher is called with the right URL.
 * 2. The result from the dispatcher call is returned(unchanged) to the caller.
 *
 *
 */
class SCMTest extends FlatSpec with OneAppPerSuite with MockitoSugar with MockitoUtils with BeforeAndAfter {

  val mockedMethod = mock[(String) => WSRequestHolder]
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

  before {
    reset(mockedMethod)
    reset(mockedRequestHolder)
    reset(mockedDispatcher)
    reset(mockedWSResponse)
  }

  "SCM#commits" should "request commits from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commits(github, project, repository, None, None)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  it should "request commits from stash API" in {
    val url = s"https://$stash/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commits(stash, project, repository, since, until)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#commit" should "request a single commit from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commit/$id"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commit(github, project, repository, id)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }
  it should "request a single commit from stash API" in {
    val url = s"https://$stash//rest/api/1.0/projects/$project/repos/$repository/commits/$id"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commit(github, project, repository, id)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#repo" should "request a single repository from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commit/$id"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repo(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }
  it should "request a single repository from stash API" in {
    val url = s"https://$stash/rest/api/1.0/projects/$project/repos/$repository"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repo(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#tickets" should "request a commit from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.tickets(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)

  }
  it should "request a commit from stash API " in {
    val url = s"https://$stash/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.tickets(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)

  }

  "SCM#repoUrl" should "return a repository-url for github API" in {
    val url = s"https://api.$github/$project/$repository"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repoUrl(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }
  it should "return a repository-url for stash API" in {
    val url = s"https://$stash/projects/$project/repos/$repository/browse"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repoUrl(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#diffUrl" should "return a diffUrl for github frontend" in {
    val url = s"https://$stash/projects/$project/repos/$repository/browse"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repoUrl(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }
  it should "return a diffUrl for stash frontend" in {
    val url = s"https://$stash/projects/$project/repos/$repository/browse"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.repoUrl(github, project, repository)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#head" should "" in {
    val url = s"Test"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.head(url)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)

  }

}