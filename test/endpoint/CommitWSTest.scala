package endpoint

import java.net.URLEncoder
import scala.{ Left, Right }
import scala.concurrent.Future
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import client.RequestDispatcher
import client.oauth.OAuth
import configuration.OAuthConfiguration
import dao.CommitRepository
import model.{ CommitResult, CommitsResult }
import model.KontrollettiToJsonParser.{ commitResultWriter, commitsResultWriter }
import model.Link
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{ GET, INTERNAL_SERVER_ERROR, LOCATION, NOT_FOUND, OK, SEE_OTHER, contentAsString, contentType, defaultAwaitTimeout, header, route, status, writeableOf_AnyContentAsEmpty }
import service.Search
import test.util.{ KontrollettiOneAppPerTestWithOverrides, MockitoUtils, OAuthTestBuilder }
import dao.PagedResult
import model.Commit

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
    recordOAuthPassAuthenticationBehaviour
  }

  after {
    verifyOAuthPassAuthenticationBehaviour
  }

  def diffRoute(host: String = host, project: String = project, repository: String = repository, source: String, target: String) = s"/api/hosts/$host/projects/$project/repos/$repository/diff/$source...$target"
  def commitsRoute(host: String = host, project: String = project, repository: String = repository, sinceId: Option[String] = None, untilId: Option[String] = None, isValid: Option[Boolean] = None) = {
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

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual SEE_OTHER
      header(LOCATION, result) === Some(LOCATION -> URLEncoder.encode(link.href, "UTF-8"))
      header(X_TOTAL_COUNT, result) mustBe None
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

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual NOT_FOUND
      header(LOCATION, result) mustBe empty
      header(X_TOTAL_COUNT, result) mustBe None
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

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual INTERNAL_SERVER_ERROR
      header(LOCATION, result) mustBe empty
      header(X_TOTAL_COUNT, result) mustBe None
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
      val commitResult = Future.successful(new PagedResult(commits, 1))
      val url = commitsRoute(sinceId = sinceId, untilId = untilId)
      when(commitRepository.get(host, project, repository, since = sinceId, until = untilId, valid = None, pageNumber = None, perPage = None)).thenReturn(commitResult)
      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual OK
      header(X_TOTAL_COUNT, result) mustBe Some(1.toString())
      contentType(result) mustEqual Some("application/x.zalando.commit+json")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
    }

    "Return 404 when objects are not found" in {
      val commitResult = Future.successful(new PagedResult[Commit](Seq(), 0))
      val url = commitsRoute(sinceId = sinceId, untilId = untilId)
      when(commitRepository.get(host, project, repository, since = sinceId, until = untilId, valid = None, pageNumber = None, perPage = None)).thenReturn(commitResult)
      val Some(result) = route(FakeRequest(GET, url).withHeaders(authorizationHeader))
      header(X_TOTAL_COUNT, result) mustBe None
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
      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual OK
      header(X_TOTAL_COUNT, result) mustBe None
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
      val Some(result) = route(FakeRequest(GET, url).withHeaders(authorizationHeader))
      header(X_TOTAL_COUNT, result) mustBe None
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustBe empty
    }

  }

  override def overrideModules = {
    Seq(
      bind[Search].toInstance(search),
      bind[CommitRepository].toInstance(commitRepository), //
      bind[OAuthConfiguration].toInstance(oauthConfig), //
      bind[RequestDispatcher].toInstance(dispatcher), //
      bind[OAuth].toInstance(oauthClient) //
      )
  }
}