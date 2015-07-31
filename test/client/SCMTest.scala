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

  "SCM#commits" should "return commits from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commits(github, project, repository, None, None)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }
  
  it should "return commits from stash API" in {
    val url = s"https://$stash/repos/$project/$repository/commits"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commits(stash, project, repository, since, until)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#commit" should "return a single commit from github API" in {
    val url = s"https://api.$github/repos/$project/$repository/commit/$id"
    when(mockedDispatcher.requestHolder(url)).thenReturn(mockedRequestHolder)
    val result = client.commit(github, project, repository, id)
    assert(result == mockedResponse)
    verify(mockedDispatcher, times(1)).requestHolder(url)
  }

  "SCM#repos" should "" in {

  }

  "SCM#tickets" should "" in {

  }

  "SCM#repoUrl" should "" in {

  }

  "SCM#diffUrl" should "" in {

  }
  "SCM#head" should "" in {

  }

}