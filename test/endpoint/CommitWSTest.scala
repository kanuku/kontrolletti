package endpoint

import java.net.URLEncoder
import scala.Right
import scala.concurrent.Future
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import dao.CommitRepository
import model.Link
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.SEE_OTHER
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.header
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import service.Search
import play.api.inject.bind
import test.util.MockitoUtils
import play.api.inject.Module
import play.api.Environment
import play.api.Configuration
import java.net.URLEncoder
import scala.concurrent._
import scala.concurrent.Future
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import model.Commit
import model.CommitsResult
import model.Link
import model.Error
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json._
import service.Search
import test.util.MockitoUtils
import model.KontrollettiToJsonParser._
import model.CommitResult
import test.util.KontrollettiOneAppPerTestWithOverrides
import test.util.OAuthTestBuilder
import dao.RepoRepository
import client.RequestDispatcher
import configuration.OAuthConfiguration

class CommitWSTest extends PlaySpec with KontrollettiOneAppPerTestWithOverrides with MockitoSugar with MockitoUtils with BeforeAndAfter with OAuthTestBuilder {
  val host = "github.com"
  val project = "zalando"
  val repository = "kontrolletti"
  val sinceId = Some("sinceId")
  val untilId = Some("untilId")
  val search = mock[Search]
  val commitRepository = mock[CommitRepository]

  before {
    reset(search, commitRepository)
    recordOAuthBehaviour
  }

  after {
    verifyOAuthBehaviour
  }

  def diffRoute(host: String = host, project: String = project, repository: String = repository, source: String, target: String) = s"/api/hosts/$host/projects/$project/repos/$repository/diff/$source...$target"
  def commitsRoute(host: String = host, project: String = project, repository: String = repository, sinceId: Option[String] = None, untilId: Option[String] = None) = {
    val since = sinceId.getOrElse("default")
    val until = untilId.getOrElse("deafult")
    s"/api/hosts/github.com/projects/zalando/repos/kontrolletti/commits?since=$since&until=$until"
  }
  def singleCommitRoute(host: String = host, project: String = project, repository: String = repository, commitId: String = "commitId") = {

    s"/api/hosts/$host/projects/$project/repos/$repository/commits/$commitId"
  }

  /**
   * Tests for Diffs starts here
   */

  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/diff/{source}...{target}" should {
    "Redirect (303) to the right scm GUI " in {

      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Link("http://link", "", "", "")
      val diffExistsResult = Future.successful(Right(Some(link)))

      when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual SEE_OTHER
      header(LOCATION, result) === Some(LOCATION -> URLEncoder.encode(link.href, "UTF-8"))
      contentAsString(result) mustBe empty

      verify(search, times(1)).diff(host, project, repository, source, target)
    }

    "Return 404 if the diff cannot be found" in {
      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Right(None)
      val diffExistsResult = Future.successful(link)

      when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual NOT_FOUND
      header(LOCATION, result) mustBe empty
      contentAsString(result) mustBe empty
      verify(search, times(1)).diff(host, project, repository, source, target)
    }

    "Return 500 if the (301) to the right scm GUI " in {
      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Left("error")
      val diffExistsResult = Future.successful(link)

      when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual INTERNAL_SERVER_ERROR
      header(LOCATION, result) mustBe empty
      contentAsString(result) mustBe empty
      contentType(result) mustEqual Some("application/problem+json")
      verify(search, times(1)).diff(host, project, repository, source, target)
    }
  }

  /**
   * Tests for Commits starts here
   */
  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/commits" should {

    "Return 200 when objects are found" in {
      val commit = createCommit()
      val commits = List(commit)
      val response = new CommitsResult(List(), commits)
      val commitResult = Future.successful(commits)
      val url = commitsRoute(sinceId = sinceId, untilId = untilId)
      when(commitRepository.get(host, project, repository)).thenReturn(commitResult)
      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual OK
      contentType(result) mustEqual Some("application/x.zalando.commit+json")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
    }

    "Return 404 when objects are not found" in {
      val commitResult = Future.successful(List())
      val url = commitsRoute(sinceId = sinceId, untilId = untilId)
      when(commitRepository.get(host, project, repository)).thenReturn(commitResult)
      val Some(result) = route(FakeRequest(GET, url))
      status(result) mustEqual NOT_FOUND
    }

  }
  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/commits/{id}" should {
    "Return 200 when the object is found" in {
      val commitId = "commitId"
      val commit = createCommit(id = commitId)
      val commitResult = Future.successful(Some(commit))
      val url = singleCommitRoute(commitId = commitId)
      val response = new CommitResult(List(), commit)
      when(commitRepository.byId(host, project, repository, commitId)).thenReturn(commitResult)
      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual OK
      contentType(result) mustEqual Some("application/x.zalando.commit+json")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
    }

    "Return 404 when the object is not found" in {
      val commitId = "commitId"
      val commit = createCommit(id = commitId)
      val commitResult = Future.successful(None)
      val url = singleCommitRoute(commitId = commitId)
      val response = new CommitResult(List(), commit)
      when(commitRepository.byId(host, project, repository, commitId)).thenReturn(commitResult)
      val Some(result) = route(FakeRequest(GET, url))
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustBe empty
    }

  }

  override def overrideModules = {
    Seq(
      bind[Search].toInstance(search),
      bind[CommitRepository].toInstance(commitRepository), //
      bind[OAuthConfiguration].toInstance(oauthConfig), //
      bind[RequestDispatcher].toInstance(dispatcher) //
      )
  }
}